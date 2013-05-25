#ifndef INIT_EXCEPTION_H
#define INIT_EXCEPTION_H

#include <iostream>
#include <exception>
using namespace std;

class InitException: public exception
{
public:
    InitException(const char* msg);
    
    virtual const char* what() const throw();
    
private:
    const char* msg;
};

#endif // INIT_EXCEPTION_H