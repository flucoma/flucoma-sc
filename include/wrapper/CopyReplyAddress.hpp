#pragma once

#include <SC_PlugIn.h>

namespace fluid{
namespace client{
    
void* copyReplyAddress(InterfaceTable* ft, World* inWorld, void* inreply);
void deleteReplyAddress(InterfaceTable* ft, World* inWorld, void* inreply);
void* copyReplyAddress(void* inreply);
void deleteReplyAddress(void* inreply);
void SendReply(void* inReplyAddr, char* inBuf, int inSize);


}  
}
