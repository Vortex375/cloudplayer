#include "InitException.h"

#include <string.h>

InitException::InitException(char* msg)
{
    this->msg = strdup(msg);
}


const char* InitException::what() const
{
    return msg;
}
