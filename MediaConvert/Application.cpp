#include "Application.h"

#include <iostream>

#include <QDebug>
#include <QFile>

Application::Application(int& argc, char** argv, int ): QCoreApplication(argc, argv)
{
  mediaConvert = new MediaConvert();
  
  QFile *inFile = new QFile();
  inFile->open(stdin, QIODevice::ReadOnly);
  
  inputReader = new InputReader(inFile);
  inputThread = new QThread();
  inputReader->moveToThread(inputThread);
    
  // quit application on error
  connect(mediaConvert, SIGNAL(error(char*)), this, SLOT(onError(char*)));
  // receive message (test)
  connect(inputReader, SIGNAL(message(QString)), this, SLOT(testMessage(QString)));
  
  inputThread->start();
}

Application::~Application()
{
    inputReader->stop();
    inputThread->wait();
    
    delete inputThread;
    delete inputReader;
    delete mediaConvert;
}

void Application::onError(char* msg)
{
  std::cout << "Fatal Error: " << msg << std::endl;
  this->exit(1);
}

void Application::testMessage(QString msg)
{
    qDebug() << msg;
}


#include "Application.moc"
