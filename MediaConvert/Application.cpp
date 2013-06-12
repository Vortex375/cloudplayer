#include "Application.h"

#include <iostream>
#include <functional>

#include <QDebug>
#include <QFile>

Application::Application(int& argc, char** argv, int ): QCoreApplication(argc, argv)
{
  mediaConvert = new MediaConvert();
  
  inputReader = new InputReader();
  inputThread = new QThread();
  inputReader->moveToThread(inputThread);
    
  // quit application on error
  connect(mediaConvert, SIGNAL(error(char*)), this, SLOT(onError(char*)));
  // receive message 
  connect(inputReader, SIGNAL(message(QString)), this, SLOT(onMessage(QString)));
  
  
  connect(inputThread, SIGNAL(started()), inputReader, SLOT(start()));
  inputThread->start();
}

Application::~Application()
{ 
    delete inputThread;
    delete inputReader;
    delete mediaConvert;
}

void Application::quit()
{
    qDebug() << "exiting...";
    mediaConvert->reset();
    inputReader->stop();
    inputThread->quit();
    inputThread->wait();
    QCoreApplication::quit();
}


void Application::onError(char* msg)
{
  std::cout << "Fatal Error: " << msg << std::endl;
  this->exit(1);
}

void Application::onMessage(QString msg)
{
    qDebug() << msg << " compare: " << QString::compare(msg, QString("q"));
    if (QString::compare(msg, QString("q")) == 0) {
        quit();
    }
}

void Application::signal_handler(int signal)
{
    ((Application *) QCoreApplication::instance())->quit();
}


#include "Application.moc"
