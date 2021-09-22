#pragma once 

#include "ArgsFromClient.hpp"
#include "ArgsToClient.hpp"
#include "CopyReplyAddress.hpp"
#include <scsynthsend.h>

namespace fluid {
namespace client {

template<typename FluidSCWrapper, typename Client>
struct FluidSCMessaging{
   
  static auto getInterfaceTable(){ return FluidSCWrapper::getInterfaceTable(); }
  static auto getName(){ return FluidSCWrapper::getName(); }

  using Params = typename Client::ParamSetType;
  using ParamValues = typename Params::ValueTuple;

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
    void*                   replyAddr{nullptr};
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

  static void refreshParams(Params& p, MessageResult<ParamValues>& r)
  {
    p.fromTuple(ParamValues(r));
  }
  
  template<typename T>
  static void refreshParams(Params&,MessageResult<T>&){}

  template<size_t N>
  static void doMessage(World* inWorld, void* inUserData, struct sc_msg_iter* args, void* replyAddr)
  {
    using MessageData = MessageDispatchCmd<N>;
    
    auto msg = new MessageData();
    
    msg->id = args->geti();
    msg->replyAddr = copyReplyAddress(replyAddr);
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
    
    auto ft = getInterfaceTable();
    
    ft->fDoAsynchronousCommand(inWorld, replyAddr, getName(), msg,
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
            else
              refreshParams(ptr->mParams, m->result);
          } else FluidSCWrapper::printNotFound(m->id);

          return true;
        },
        [](World* world, void* data) // RT thread:  buffer swap (and possible completion messages)
        {
          MessageData* m = static_cast<MessageData*>(data);
          MessageData::Descriptor::template forEachArg<typename BufferT::type,
                                                 impl::AssignBuffer>(m->args,
                                                                     world);
          return true;
        },
        [](World*, void* data) // NRT Thread: Send reply
        {
          MessageData* m = static_cast<MessageData*>(data);
          if(m->result.status() != Result::Status::kError)
            messageOutput(m->name, m->id, m->result, m->replyAddr);
          return false;
        },
        [](World*, void* data) // RT thread: clean up
        {
          MessageData* m = static_cast<MessageData*>(data);
          delete m;
        },
        static_cast<int>(completionMsgSize), completionMsgData);
        
        if(completionMsgSize) ft->fRTFree(inWorld, completionMsgData);
        
  }
  
  template <size_t N, typename ArgsTuple, size_t... Is> // Call from NRT
  static decltype(auto) invokeImpl(Client& x, ArgsTuple& args,
                                   std::index_sequence<Is...>)
  {
    return x.template invoke<N>(x, std::get<Is>(args)...);
  }
  
  template <typename T> // call from RT
  static void messageOutput(const std::string& s, index id, MessageResult<T>& result, void* replyAddr)
  {
    index  numTags = ToOSCTypes<small_scpacket>::numTags(static_cast<T>(result));
    if(numTags > 2048)
    {
      std::cout << "ERROR: Message response too big to send (" << asUnsigned(numTags) * sizeof(float) << " bytes)." << std::endl;
      return;
    }

    small_scpacket packet;
    packet.adds(s.c_str());
    packet.maketags(static_cast<int>(numTags) + 2);
    packet.addtag(',');
    packet.addtag('i');
    ToOSCTypes<small_scpacket>::getTag(packet, static_cast<T>(result));
    
    packet.addi(static_cast<int>(id));
    ToOSCTypes<small_scpacket>::convert(packet, static_cast<T>(result));
    
    if(replyAddr)
      SendReply(replyAddr,packet.data(),static_cast<int>(packet.size()));
  }

  static void messageOutput(const std::string& s,index id, MessageResult<void>&, void* replyAddr)
  {
    small_scpacket packet;
    packet.adds(s.c_str());
    packet.maketags(2);
    packet.addtag(',');
    packet.addtag('i');
    packet.addi(static_cast<int>(id));
    
    if(replyAddr)
      SendReply(replyAddr,packet.data(),static_cast<int>(packet.size()));
  }

  template <typename... Ts>
  static void messageOutput(const std::string& s, index id, MessageResult<std::tuple<Ts...>>& result, void* replyAddr)
  {
    using T = std::tuple<Ts...>;
    
    index  numTags = ToOSCTypes<small_scpacket>::numTags(static_cast<T>(result));
    if(numTags > 2048)
    {
      std::cout << "ERROR: Message response too big to send (" << asUnsigned(numTags) * sizeof(float) << " bytes)." << std::endl;
      return;
    }
    
    small_scpacket packet;
    packet.adds(s.c_str());
    packet.maketags(static_cast<int>(numTags + 3));
    packet.addtag(',');
    packet.addtag('i');
    ToOSCTypes<small_scpacket>::getTag(packet,static_cast<T>(result));
    
    packet.addi(static_cast<int>(id));
    ToOSCTypes<small_scpacket>::convert(packet, static_cast<T>(result));
    
    if(replyAddr)
      SendReply(replyAddr,packet.data(),static_cast<int>(packet.size()));

  }
};
}
}
