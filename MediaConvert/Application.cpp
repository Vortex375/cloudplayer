#include "Application.h"

#include <iostream>
#include <functional>

#include <QDebug>
#include <QFile>

Application::Application(int& argc, char** argv, int ): QCoreApplication(argc, argv)
{
    if (argc < 2) {
        std::cerr << "Missing input filename." << std::endl;
        std::exit(1);
    }
    
    mediaConvert = new MediaConvert(argv[1]);
    
    if (argc >= 3) {
        double offset = atof(argv[2]);
        mediaConvert->pause();
        mediaConvert->seek(offset);
    }

    //inputReader = new InputReader();
    //inputThread = new QThread();
    //inputReader->moveToThread(inputThread);

    // quit application on error
    connect(mediaConvert, SIGNAL(error(char*)), this, SLOT(onError(char*)));
    // receive message
    //connect(inputReader, SIGNAL(message(QString)), this, SLOT(onMessage(QString)));


    //connect(inputThread, SIGNAL(started()), inputReader, SLOT(start()));
    //inputThread->start();
    
    mediaConvert->play();
}

Application::~Application()
{
    //delete inputThread;
    //delete inputReader;
    delete mediaConvert;
}

void Application::quit()
{
    //qDebug() << "exiting...";
    mediaConvert->reset();
    //inputReader->stop();
    //inputThread->quit();
    //inputThread->wait();
    QCoreApplication::quit();
}


void Application::onError(char* msg)
{
    std::cout << "Fatal Error: " << msg << std::endl;
    this->exit(1);
}

void Application::onMessage(QString msg)
{
    //qDebug() << msg << " compare: " << QString::compare(msg, QString("q"));
    if (QString::compare(msg, QString("q")) == 0) {
        quit();
    } else if (QString::compare(msg, QString("play")) == 0) {
        mediaConvert->play();
    } else if (QString::compare(msg, QString("pause")) == 0) {
        mediaConvert->pause();
    } else if (msg.startsWith(QString("seek:"))) {
        // parse seek
        QByteArray data = msg.toLocal8Bit();
        //qDebug() << "seek data: " << data.constData() + 5;
        double seconds = atof(data.constData() + 5);
        mediaConvert->seek(seconds);
    }
}

void Application::signal_handler(int signal)
{
    ((Application *) QCoreApplication::instance())->quit();
}


#include "Application.moc"
