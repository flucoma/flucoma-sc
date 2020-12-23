#pragma once

#include <SC_ReplyImpl.hpp>

namespace fluid{
namespace client{
    
void* copyReplyAddress(InterfaceTable* ft, World* inWorld, void* inreply)
{
  
  if(! inreply) return nullptr; 
  
  ReplyAddress* reply = (ReplyAddress*)ft->fRTAlloc(inWorld, sizeof(ReplyAddress)); 
  
  *reply = *(static_cast<ReplyAddress*>(inreply));
  
  return reply;   
}

void deleteReplyAddress(InterfaceTable* ft, World* inWorld, void* inreply)
{
  if(! inreply) return;
  ft->fRTFree(inWorld,(ReplyAddress*)inreply);
}

void* copyReplyAddress(void* inreply)
{
  
  if(! inreply) return nullptr; 
  
  ReplyAddress* reply = new ReplyAddress(); 
  
  *reply = *(static_cast<ReplyAddress*>(inreply));
  
  return reply;   
}

void deleteReplyAddress(void* inreply)
{
  if(! inreply) return;
  delete (ReplyAddress*)inreply;
}



}  
}
