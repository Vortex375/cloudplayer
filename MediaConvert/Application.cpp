#include "Application.h"

#include <iostream>

Application::Application(int& argc, char** argv, int ): QCoreApplication(argc, argv)
{
  MediaConvert mediaConvert;
    
  // quit application on error
  connect(&mediaConvert, SIGNAL(error(char*)), this, SLOT(onError(char*)));
}

Application::~Application()
{

}

void Application::onError(char* msg)
{
  std::cout << "Fatal Error: " << msg << std::endl;
  this->exit(1);
}

#include "Application.moc"
