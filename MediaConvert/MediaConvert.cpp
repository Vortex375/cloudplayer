#include "MediaConvert.h"

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
    default:
        break;
    }

    return TRUE;
}

MediaConvert::MediaConvert() throw (InitException)
{
    mPipeline = NULL;

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
    gst_element_set_state(mPipeline, GST_STATE_PLAYING);
}

void MediaConvert::reset()
{
    if (mPipeline) {
        // reset pipeline
        gst_element_set_state(mPipeline, GST_STATE_NULL);
        
        // free the current pipeline and construct a new one, just to be safe
        gst_object_unref(mPipeline);
        
        // disconnect bus message handler
        g_source_remove(mBusWatch);
    }

    GError* error = NULL;

    mPipeline = gst_parse_launch(MEDIACONVERT_TRANSCODE_PIPELINE, &error);

    if (!mPipeline) {
        emitError("unable to reset pipeline");
        return;
    }
    
    // connect bus message handler
    GstBus *bus = gst_pipeline_get_bus((GstPipeline*) mPipeline);
    mBusWatch = gst_bus_add_watch(bus, &busCall, this);
    gst_object_unref(bus);
}

void MediaConvert::seek(double seconds)
{
    gst_element_seek_simple(mPipeline, GST_FORMAT_TIME, GST_SEEK_FLAG_FLUSH, (gint64) (seconds * GST_SECOND));
}


#include "MediaConvert.moc"
