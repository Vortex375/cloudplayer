#include "MediaConvert.h"

#include <QDebug>

gboolean MediaConvert::busCall(GstBus* bus, GstMessage* msg, gpointer data)
{
    MediaConvert* that = (MediaConvert *) data;

    switch (GST_MESSAGE_TYPE (msg)) {

    case GST_MESSAGE_ERROR: {
        gchar  *debug = NULL;
        GError *error = NULL;

        gst_message_parse_error (msg, &error, &debug);
        g_free (debug);

        g_printerr ("Error: %s\n", error->message);
        that->emitError(error->message);
        g_error_free (error);
        break;
    }
    /*case GST_MESSAGE_STATE_CHANGED: {
        if (GST_MESSAGE_SRC(msg) == (GstObject *) that->mPipeline) {
            GstState oldState;
            GstState newState;
            GstState pending;
            
            gst_message_parse_state_changed(msg, &oldState, &newState, &pending);
            
            if (newState == GST_STATE_PAUSED && that->seekPending) {
                gst_element_seek_simple(that->mPipeline, GST_FORMAT_TIME, (GstSeekFlags) (GST_SEEK_FLAG_FLUSH | GST_SEEK_FLAG_KEY_UNIT), (gint64) (that->seekPos * GST_SECOND));
                that->seekPending = false;
            }
        }
    }*/
    default:
        break;
    }

    return TRUE;
}

MediaConvert::MediaConvert(char* infile) throw (InitException)
{
    this->infile = infile;
    
    mPipeline = NULL;
    seekPending = false;

    reset();

    if (!mPipeline) {
        // pipeline construction failed
        throw InitException("Unable to construct pipeline");
    }


}
MediaConvert::~MediaConvert()
{

}

void MediaConvert::emitError(char* msg)
{
    emit error(msg);
}

void MediaConvert::pause()
{
    gst_element_set_state(mPipeline, GST_STATE_PAUSED);
}

void MediaConvert::play()
{
    /*gst_bin_add((GstBin *) mPipeline, (GstElement *) encodeBin);
    GstElement* queue = gst_bin_get_by_name((GstBin *) mPipeline, "encQueue");
    GstElement* sink = gst_bin_get_by_name((GstBin *) mPipeline, "encSink");
    GstElement* enc = gst_bin_get_by_name(encodeBin, "enc");
    GstElement* mux = gst_bin_get_by_name(encodeBin, "mux");
    
    gst_element_unlink(queue, sink);
    gst_element_link(queue, enc);
    gst_element_link(mux, sink);
    
    gst_object_unref(queue);
    gst_object_unref(sink);
    gst_object_unref(enc);
    gst_object_unref(mux);
    
    gst_element_sync_state_with_parent((GstElement *) encodeBin);*/
    gst_element_set_state(mPipeline, GST_STATE_PLAYING);
}

void MediaConvert::reset()
{
    seekPending = false;
    GValue val = G_VALUE_INIT;
    
    if (mPipeline) {
        // reset pipeline
        gst_element_set_state(mPipeline, GST_STATE_NULL);
        
        // free the current pipeline and construct a new one, just to be safe
        gst_object_unref(mPipeline);
        mPipeline = NULL;
        
        // disconnect bus message handler
        g_source_remove(mBusWatch);
    }

    GError* error = NULL;

    // construct pipeline
    mPipeline = gst_parse_launch(MEDIACONVERT_TRANSCODE_PIPELINE, &error);
    if (!mPipeline) {
        emitError("unable to reset pipeline");
        return;
    }
    
    // set source property
    GstElement* src = gst_bin_get_by_name((GstBin*) mPipeline, "src");
    g_value_init(&val, G_TYPE_STRING);
    g_value_set_string(&val, infile);
    g_object_set_property((GObject*) src, "location", &val);
    g_value_unset(&val);
    gst_object_unref(src);
    
    // create encode bin
    /*encodeBin = (GstBin *) gst_bin_new("encodeBin");
    
    GstElement* enc = gst_element_factory_make("vorbisenc", "enc");
    //val = G_VALUE_INIT;
    g_value_init(&val, G_TYPE_DOUBLE);
    g_value_set_double(&val, 0.6);
    g_object_set_property((GObject *) enc, "quality", &val);
    g_value_unset(&val);
    
    GstElement* mux = gst_element_factory_make("webmmux", "mux");
    //val = G_VALUE_INIT;
    g_value_init(&val, G_TYPE_BOOLEAN);
    g_value_set_boolean(&val, true);
    g_object_set_property((GObject *) mux, "streamable", &val);
    g_value_unset(&val);
    
    gst_bin_add_many(encodeBin, enc, mux, NULL);
    gst_element_link_many(enc, mux, NULL);*/
    
    
    // connect bus message handler
    GstBus *bus = gst_pipeline_get_bus((GstPipeline*) mPipeline);
    mBusWatch = gst_bus_add_watch(bus, &busCall, this);
    gst_object_unref(bus);
}

void MediaConvert::seek(double seconds)
{
    //qDebug() << "seeking to " << seconds << " ("<< (seconds * GST_SECOND) << ")";
    //gst_element_set_state(mPipeline, GST_STATE_NULL);
    //GstElement* queue = gst_bin_get_by_name((GstBin*) mPipeline, "queue");
    //GstElement* conv = gst_bin_get_by_name((GstBin*) mPipeline, "conv");
    //GstElement* src = gst_bin_get_by_name((GstBin*) mPipeline, "src");
    
    //gst_element_unlink(queue, conv);
    //gst_bin_add((GstBin*) mPipeline, fakesink);
    //gst_element_link(queue, fakesink);
    //gst_element_sync_state_with_parent(fakesink);
    
    //seekPending = true;
    //seekPos = seconds;
    gst_element_seek_simple(mPipeline, GST_FORMAT_TIME, (GstSeekFlags) (GST_SEEK_FLAG_FLUSH | GST_SEEK_FLAG_KEY_UNIT), (gint64) (seconds * GST_SECOND));
    //gst_object_ref(fakesink);
    //gst_bin_remove((GstBin*) mPipeline, fakesink);
    //gst_element_link(queue, conv);
    
    //gst_object_unref(queue);
    //gst_object_unref(conv);
    //gst_object_unref(src);
}


#include "MediaConvert.moc"
