#include <QtCore/QCoreApplication>
#include <QtCore/QStringList>
#include <QtCore/QVector>
#include <QtCore/QTextStream>
#include <QtCore/QThread>
#include <QtCore/QDir>

#include <iostream>
#include <stdio.h>

#include<taglib/fileref.h>
#include<taglib/tag.h>

#include<boost/filesystem.hpp>

#include "Stats.h"
#include "BlockingQueue.h"
#include "DirScanner.h"
#include "Indexer.h"
#include "ProgressOutput.h"
#include "Database.h"
#include "Updater.h"
#include "Covers.h"

using namespace boost::filesystem;

static QTextStream out(stdout);

void printUsage() {
    out << "Usage:" << endl;
    out << "\t mediaindex <create|update> <database file> <media directories ...>" << endl;
    out << "\t mediaindex covers <database file>" << endl;
    out << "\t mediaindex dump_covers <database file> <output directory>" << endl;
}

void createDatabase(char* databasePath, char* dirPath) {
    out << "Creating new database at " << databasePath << endl;
    // check if database file already exists
    if (exists(databasePath)) {
        out << "Error: database file already exists!" << endl;
        out << "'create' creates a new database file. "
            << "If you wish to update an existing database use 'update'." << endl;
        return;
    }

    Stats stats;
    BlockingQueue pathQueue;

    DirScanner scanner(&pathQueue, &stats, dirPath);
    Indexer indexer(&pathQueue, &stats, databasePath);

    QThread *outputThread = new QThread();
    ProgressOutput *output = new ProgressOutput(&stats);
    output->moveToThread(outputThread);

    scanner.start();
    out << "Filesystem scan started." << endl;
    indexer.start();
    out << "Indexing started." << endl << endl;
    outputThread->start();

    scanner.wait();
    //out << endl << "Filesystem scan complete." << endl << endl;
    indexer.wait();
    outputThread->quit();
    out << endl << endl << "Indexing complete." << endl;
}

void updateDatabase(char *databasePath, char *dirPath) {
    out << "Open database at " << databasePath << endl;
    // check if database file exists
    if (!exists(databasePath)) {
        out << "Error: database file does not exist!" << endl;
        out << "'update' updates an existing database file. "
            << "If you wish to create a new database use 'create'." << endl;
        return;
    }

    Stats stats;
    BlockingQueue pathQueue;

    DirScanner scanner(&pathQueue, &stats, dirPath);
    Updater updater(&pathQueue, &stats, databasePath);

    QThread *outputThread = new QThread();
    ProgressOutput *output = new ProgressOutput(&stats);
    output->moveToThread(outputThread);

    scanner.start();
    out << "Filesystem scan started." << endl;
    updater.start();
    out << "Indexing started." << endl << endl;
    outputThread->start();

    scanner.wait();
    //out << endl << "Filesystem scan complete." << endl << endl;
    updater.wait();
    outputThread->quit();
    out << endl << endl << "Indexing complete." << endl;
}

void setCovers(char *databasePath) {
    out << "Open database at " << databasePath << endl;
    // check if database file exists
    if (!exists(databasePath)) {
        out << "Error: database file does not exist!" << endl;
        return;
    }

    Stats stats;

    Covers covers(&stats, databasePath);

    QThread *outputThread = new QThread();
    ProgressOutput *output = new ProgressOutput(&stats);
    output->moveToThread(outputThread);


    out << "Adding covers to database." << endl;
    covers.start();
    outputThread->start();

    covers.wait();
    outputThread->quit();
    out << endl << endl << "Finished." << endl;
}

void dumpCovers(char *databasePath, char *outputDir) {
    QDir().mkpath(QString::fromUtf8(outputDir));

    // open database
    Database db;
    if (!db.open(databasePath)) {
        out << "Error: failed to open database." << endl;
        exit(1);
        return;
    };
    
    out << "Dumping covers to " << outputDir << endl << endl;

    DbCoverIterator iter = db.getAllCovers();
    while(iter.next()) {
        QString hash = iter.getHash();
        QString mime = iter.getMimeType();
        QByteArray data = iter.getCover();
        
        QFile outfile(QString(outputDir).append("/").append(hash));
        outfile.open(QIODevice::WriteOnly);
        outfile.write(data);
        outfile.flush();
        outfile.close();
        out << "Wrote " << outfile.fileName() << " (" << mime << ")" << endl;
    }
    
    out << endl << endl << "Finished." << endl;
}

int main(int argc, char** argv) {
    if (sqlite3_config(SQLITE_CONFIG_MULTITHREAD) != SQLITE_OK) {
        out << "Error: unable to configure sqlite for multi-thread mode.";
        exit(1);
    }

    QCoreApplication app(argc, argv);
    out << "Welcome to MediaIndex v0.1." << endl << endl;

    if (argc < 2) {
        // missing command
        printUsage();
        return 0;
    }

    if (!strcmp(argv[1], "create")) {
        if (argc < 4) {
            printUsage();
            return 0;
        }
        createDatabase(argv[2], argv[3]);

    } else if (!strcmp(argv[1], "update")) {
        if (argc < 4) {
            printUsage();
            return 0;
        }
        updateDatabase(argv[2], argv[3]);

    } else if (!strcmp(argv[1], "covers")) {
        if (argc < 3) {
            printUsage();
            return 0;
        }
        setCovers(argv[2]);

    } else if (!strcmp(argv[1], "dump_covers")) {
        if (argc < 4) {
            printUsage();
            return 0;
        }
        dumpCovers(argv[2], argv[3]);

    } else {
        printUsage();
    }

    return 0;
}
