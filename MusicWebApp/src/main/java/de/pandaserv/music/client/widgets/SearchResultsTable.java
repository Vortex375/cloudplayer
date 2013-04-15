package de.pandaserv.music.client.widgets;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.view.client.*;
import de.pandaserv.music.client.i18n.GuiConstants;
import de.pandaserv.music.client.remote.MyAsyncCallback;
import de.pandaserv.music.client.remote.RemoteService;
import de.pandaserv.music.shared.RangeResponse;
import de.pandaserv.music.shared.TrackDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/8/13
 * Time: 6:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultsTable extends CellTable<TrackDetail> {
    public static interface TrackClickHandler {
        public void onTrackClicked(long trackId);
    }

    /*
     * Key provider for TrackDetail
     */
    private static final ProvidesKey<TrackDetail> KEY_PROVIDER = new ProvidesKey<TrackDetail>() {
        @Override
        public Object getKey(TrackDetail item) {
            return item.getId();
        }
    };

    /*
     * Async Data Provider
     */
    private static class TrackDataProvider extends AsyncDataProvider<TrackDetail> {
        private long queryId;
        private int count;
        private TrackDetail[] initialData;

        private TrackDataProvider() {
            count = 0; // empty
        }

        public void setParameters(long queryId, int count, TrackDetail[] initialData) {
            GWT.log("TrackDataProvider: new data: count=" + count + " initial data length=" + initialData.length);
            this.queryId = queryId;
            this.count = count;
            this.initialData = initialData;
        }

        @Override
        protected void onRangeChanged(HasData<TrackDetail> display) {
            if (count == 0) {
                /*
                 * there is no data to provide
                 */
                return;
            }

            /*
             * get visible range
             */
            Range visibleRange = display.getVisibleRange();
            int start = visibleRange.getStart();
            int end = start + visibleRange.getLength();

            /*
             * truncate range to the length of available data
             */
            if (start < 0) {
                start = 0;
            }
            if (end > count) {
                end = count;
            }

            GWT.log("TrackDataProvider: getData start=" + start + " end=" + end);

            if (start < initialData.length && end < initialData.length) {
                /*
                 * we can use the data from the initialData array
                 * no need to make a request
                 */
                TrackDetail[] data = new TrackDetail[end - start];
                System.arraycopy(initialData, start, data, 0, data.length);
                updateRowData(start, Arrays.asList(data));
            } else {
                /*
                 * make request
                 */
                final int requestStart = start;
                RemoteService.getInstance().getTrackDetailRange(queryId, new Range(start, end - start), new MyAsyncCallback<RangeResponse<TrackDetail>>() {
                    @Override
                    protected void onResult(RangeResponse<TrackDetail> result) {
                        if (result == null) {
                            // call failed
                            // we just silently discard this error here
                            // probably bad
                            return;
                        }
                        updateRowData(requestStart, Arrays.asList(result.getData()));
                    }
                });
            }
        }
    }

    /*
     * i18n messages
     */
    private static final GuiConstants msg = GWT.create(GuiConstants.class);

    private static final int PAGE_INCREMENT = 30; // how many new rows to add when calling fetchMore()

    private final Column<TrackDetail, String> titleColumn;
    private final Column<TrackDetail, String> artistColumn;
    private final Column<TrackDetail, String> albumColumn;

    private final TrackDataProvider dataProvider;
    private final MultiSelectionModel<TrackDetail> selectionModel;
    private final List<TrackClickHandler> trackClickHandlers;

    private int visibleRows;

    public SearchResultsTable() {
        super(PAGE_INCREMENT, (CellTable.SelectableResources) GWT.create(CellTable.SelectableResources.class));
        setTableLayoutFixed(true);

        visibleRows = PAGE_INCREMENT;
        sinkEvents(Event.ONDBLCLICK);

        trackClickHandlers = new ArrayList<TrackClickHandler>();

        selectionModel = new MultiSelectionModel<TrackDetail>(KEY_PROVIDER);
        setSelectionModel(selectionModel);
        setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
        //setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

        titleColumn = new TextColumn<TrackDetail>() {
            @Override
            public String getCellStyleNames(Cell.Context context, TrackDetail object) {
                return "searchResultCell";
            }

            @Override
            public String getValue(TrackDetail trackDetail) {
                if (trackDetail == null) {
                    return null;
                }
                return trackDetail.getTitle();
            }
        };
        artistColumn = new TextColumn<TrackDetail>() {
            @Override
            public String getCellStyleNames(Cell.Context context, TrackDetail object) {
                return "searchResultCell";
            }

            @Override
            public String getValue(TrackDetail trackDetail) {
                if (trackDetail == null) {
                    return null;
                }
                return trackDetail.getArtist();
            }
        };
        albumColumn = new TextColumn<TrackDetail>() {
            @Override
            public String getCellStyleNames(Cell.Context context, TrackDetail object) {
                return "searchResultCell";
            }

            @Override
            public String getValue(TrackDetail trackDetail) {
                if (trackDetail == null) {
                    return null;
                }
                return trackDetail.getAlbum();
            }
        };

        addColumn(titleColumn, msg.title());
        addColumn(artistColumn, msg.artist());
        addColumn(albumColumn, msg.album());

        /*addColumnStyleName(0, "searchResultColumn");
        addColumnStyleName(0, "searchResultColumn");
        addColumnStyleName(0, "searchResultColumn");*/

        setColumnWidth(titleColumn, 33, com.google.gwt.dom.client.Style.Unit.PCT);
        setColumnWidth(artistColumn, 33, com.google.gwt.dom.client.Style.Unit.PCT);
        setColumnWidth(albumColumn, 33, com.google.gwt.dom.client.Style.Unit.PCT);

        setRowCount(0);
        dataProvider = new TrackDataProvider();
        dataProvider.addDataDisplay(this);


        /*
         * Handler for row double clicks
         */
        addCellPreviewHandler(new CellPreviewEvent.Handler<TrackDetail>() {
            @Override
            public void onCellPreview(CellPreviewEvent<TrackDetail> event) {
                GWT.log("onCellPreview: " + event.getNativeEvent().getType());
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    onTrackClicked(event.getValue().getId());
                } else if (event.getNativeEvent().getType().equals("dblclick")) {
                    event.getNativeEvent().preventDefault();
                    onTrackClicked(event.getValue().getId());
                }
            }
        });

        addRowCountChangeHandler(new RowCountChangeEvent.Handler() {
            @Override
            public void onRowCountChange(RowCountChangeEvent event) {
                updateVisibleRange();
            }
        });

        //bind();
    }

    public void setData(RangeResponse<TrackDetail> data) {
        setRowCount(0);  // hack to clear previous data
        visibleRows = PAGE_INCREMENT;
        updateVisibleRange();
        dataProvider.setParameters(data.getQueryId(), data.getTotalCount(), data.getData());
        setRowCount(data.getTotalCount());
    }

    public void fetchMore() {
        visibleRows += PAGE_INCREMENT;
        updateVisibleRange();
    }

    public void clear() {
        setRowCount(0);
        visibleRows = PAGE_INCREMENT;
        updateVisibleRange();
        dataProvider.setParameters(0, 0, new TrackDetail[0]);
    }

    private void updateVisibleRange() {
        GWT.log("TrackTable: updateVisibleRange(): 0, " + Math.min(getRowCount(), visibleRows));
        setVisibleRange(0, Math.min(getRowCount(), visibleRows));
    }

    public void addTrackClickHandler(TrackClickHandler handler) {
        trackClickHandlers.add(handler);
    }

    private void onTrackClicked(long id) {
        for (TrackClickHandler handler : trackClickHandlers) {
            handler.onTrackClicked(id);
        }
    }
}
