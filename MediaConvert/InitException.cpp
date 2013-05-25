#include "InitException.h"

#include <string.h>

InitException::InitException(const char* msg)
{
    this->msg = strdup(msg);
}


const char* InitException::what() const throw()
{
    return msg;
}
