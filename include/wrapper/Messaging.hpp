#pragma once 

#include "ArgsFromClient.hpp"
#include "ArgsToClient.hpp"

namespace fluid {
namespace client {

template<typename FluidSCWrapper, typename Client>
struct FluidSCMessaging{
   
  static auto getInterfaceTable(){ return FluidSCWrapper::getInterfaceTable(); }
  static auto getName(){ return FluidSCWrapper::getName(); }


  template <size_t N>
  struct MessageDispatchCmd
  {
    using Descriptor = typename Client::MessageSetType::template MessageDescriptorAt<N>;
    using ArgTuple   = typename Descriptor::ArgumentTypes;
    using ReturnType = typename Descriptor::ReturnType;
    using IndexList  = typename Descriptor::IndexList;
  
    static constexpr size_t Message = N;
    index                   id;
    ArgTuple                args;
    ReturnType              result;
    std::string             name;
    IndexList               argIndices;
  };
  

  template <size_t N, typename T>
  struct SetupMessageCmd
  {
  
    void operator()(const T& message)
    {
      static std::string messageName = std::string{getName()} + '/' + message.name;
      auto ft = getInterfaceTable();      
      ft->fDefinePlugInCmd(messageName.c_str(), doMessage<N>,(void*)messageName.c_str());
    }
  };
  

  template <typename Message>
  static bool validateMessageArgs(Message* msg, sc_msg_iter* inArgs)
  {
    using ArgTuple = decltype(msg->args);
    
    std::string tags(inArgs->tags + inArgs->count);//evidently this needs commenting: construct string at pointer offset by tag count, to pick up args
    bool willContinue = true;
    bool typesMatch = true;
    
    auto& args = msg->args;
    
    constexpr size_t expectedArgCount = std::tuple_size<ArgTuple>::value;
    
    /// TODO this squawks if we have a completion message, so maybe we can check if extra arg is a 'b' and squawk if not?
//    if(tags.size() > expectedArgCount)
//    {
//      std::cout << "WARNING: " << msg->name << " received more arguments than expected (got "
//                << tags.size() << ", expect " << expectedArgCount << ")\n";
//    }
    
    if(tags.size() < expectedArgCount)
    {
      std::cout << "ERROR: " << msg->name << " received fewer arguments than expected (got "
                << tags.size() << ", expect " << expectedArgCount << ")\n";
      willContinue = false;
    }

    auto tagsIter = tags.begin();
    auto tagsEnd  = tags.end();
    ForEach(args,[&typesMatch,&tagsIter,&tagsEnd](auto& arg){
       if(tagsIter == tagsEnd)
       {
          typesMatch = false;
          return;
       }
       char t = *(tagsIter++);
       typesMatch = typesMatch && ParamReader<sc_msg_iter>::argTypeOK(arg,t);
    });
    
   willContinue = willContinue && typesMatch;
   
   if(!typesMatch)
   {
      auto& report = std::cout;
      report << "ERROR: " << msg->name << " type signature incorrect.\nExpect: (";
      size_t i{0};
      ForEach(args, [&i](auto& x){
        std::cout << ParamReader<sc_msg_iter>::argTypeToString(x);
        if(i < (std::tuple_size<ArgTuple>::value - 1 ) )
        {
          std::cout << " ,";
        }
        i++;
      });
      report << ")\nReceived: (";
      i = 0;
      for(auto t: tags)
      {
        report << ParamReader<sc_msg_iter>::oscTagToString(t);
        if( i < ( tags.size() - 1 ) )
        {
          report << ", ";
        }
        i++;
      }
      report << ")\n";
   }
   
   return willContinue;
  }


  template<size_t N>
  static void doMessage(World* inWorld, void* inUserData, struct sc_msg_iter* args, void* replyAddr)
  {
    using MessageData = MessageDispatchCmd<N>;
    
    auto msg = new MessageData();
    
    msg->id = args->geti();
    ///TODO make this step contingent on verbosity or something, in the name of effieciency
    bool willContinue = validateMessageArgs(msg, args);
    
    if(!willContinue)
    {
      delete msg;
      return;
    }


    msg->name = std::string{'/'} +  (const char*)(inUserData);
    
    ForEach(msg-> args,[inWorld,&args](auto& thisarg)
    {
      thisarg = ParamReader<sc_msg_iter>::fromArgs(inWorld, *args,thisarg,0);
    });
     
    size_t completionMsgSize{args ? args->getbsize() : 0};
    assert(completionMsgSize <= std::numeric_limits<int>::max());
    char* completionMsgData = nullptr;
    
    if (completionMsgSize) {
      completionMsgData = (char*)getInterfaceTable()->fRTAlloc(inWorld, completionMsgSize);
      args->getb(completionMsgData, completionMsgSize);
    }
    
     
    getInterfaceTable()->fDoAsynchronousCommand(inWorld, replyAddr, getName(), msg,
        [](World* world, void* data) // NRT thread: invocation
        {
          MessageData* m = static_cast<MessageData*>(data);
          using ReturnType = typename MessageData::ReturnType;
          
          if(auto  ptr = FluidSCWrapper::get(m->id).lock())
          {
            m->result =
                ReturnType{invokeImpl<N>(ptr->mClient, m->args,m->argIndices)};

            if (!m->result.ok())
              FluidSCWrapper::printResult(world, m->result);
          } else FluidSCWrapper::printNotFound(m->id);

          return true;
        },
        [](World* world, void* data) // RT thread:  response
        {
          MessageData* m = static_cast<MessageData*>(data);
          MessageData::Descriptor::template forEachArg<typename BufferT::type,
                                                 impl::AssignBuffer>(m->args,
                                                                     world);
          return true;
        },
        nullptr,                 // NRT Thread: No-op
        [](World* world, void* data) // RT thread: clean up
        {
          MessageData* m = static_cast<MessageData*>(data);
          
          if(m->result.status() != Result::Status::kError)
            messageOutput(m->name, m->id, m->result, world);
          else
          {
             auto ft = getInterfaceTable();
             ft->fSendNodeReply(ft->fGetNode(world,0),-1, m->name.c_str(),0, nullptr);
          }
                    
          delete m;
        },
        static_cast<int>(completionMsgSize), completionMsgData);
  }
  
  template <size_t N, typename ArgsTuple, size_t... Is> // Call from NRT
  static decltype(auto) invokeImpl(Client& x, ArgsTuple& args,
                                   std::index_sequence<Is...>)
  {
    return x.template invoke<N>(x, std::get<Is>(args)...);
  }
  
  template <typename T> // call from RT
  static void messageOutput(const std::string& s, index id, MessageResult<T>& result, World* world)
  {
    auto ft = getInterfaceTable();
    
    // allocate return values
    index  numArgs = ToFloatArray::allocSize(static_cast<T>(result));

    if(numArgs > 2048)
    {
      std::cout << "ERROR: Message response too big to send (" << asUnsigned(numArgs) * sizeof(float) << " bytes)." << std::endl;
      return;
    }
        
    float* values = new float[asUnsigned(numArgs)];
    
    // copy return data
    ToFloatArray::convert(values, static_cast<T>(result));

    ft->fSendNodeReply(ft->fGetNode(world,0), static_cast<int>(id), s.c_str(),
                       static_cast<int>(numArgs), values);

    delete[] values;
  }

  static void messageOutput(const std::string& s,index id, MessageResult<void>&, World* world)
  {
    auto ft = getInterfaceTable();
    ft->fSendNodeReply(ft->fGetNode(world,0), static_cast<int>(id), s.c_str(), 0, nullptr);
  }

  template <typename... Ts>
  static void messageOutput(const std::string& s, index id, MessageResult<std::tuple<Ts...>>& result, World* world)
  {
    std::array<index, sizeof...(Ts)> offsets;
    
    index   numArgs;

    std::tie(offsets, numArgs) =
        ToFloatArray::allocSize(static_cast<std::tuple<Ts...>>(result));
    
    if(numArgs > 2048)
    {
      std::cout << "ERROR: Message response too big to send (" << asUnsigned(numArgs) * sizeof(float) << " bytes)." << std::endl;
      return;
    }
    
    float* values = new float[asUnsigned(numArgs)];
    ToFloatArray::convert(values, std::tuple<Ts...>(result), offsets,
                          std::index_sequence_for<Ts...>());

    auto  ft = getInterfaceTable();
    ft->fSendNodeReply(ft->fGetNode(world,0), static_cast<int>(id), s.c_str(),
                       static_cast<int>(numArgs), values);
    
    delete[] values;
  }
};
}
}
