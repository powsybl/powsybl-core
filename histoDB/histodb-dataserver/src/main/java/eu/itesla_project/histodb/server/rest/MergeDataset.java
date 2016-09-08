package eu.itesla_project.histodb.server.rest;

import be.pepite.dataserver.api.ColumnDescriptor;
import be.pepite.dataserver.api.FunctionalColumn;
import be.pepite.dataserver.datastores.mongodb.MongoDataset;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 17/12/13
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */
public class MergeDataset extends MongoDataset
{
    private DBCursor snapshots;

    public MergeDataset(DBCursor forecasts, DBCursor snapshots, ColumnDescriptor[] queriedFields) {
        super(forecasts, queriedFields);
        this.snapshots = snapshots;
    }

    public MergeDataset(MongoDataset forecasts, MongoDataset snapshots, ColumnDescriptor[] queriedFields) {
        this(forecasts.getCursor(), snapshots.getCursor(), queriedFields);
    }

    @Override
    public Iterator<Collection> getRowIterator() {
        return new Iterator<Collection>() {

            private DBObject nextForecast;
            private DBObject nextSnapshot;
            private boolean lastSnapShotsMatches;

            @Override
            public boolean hasNext() {
                if (nextForecast != null || nextSnapshot != null) return true;
                else return findNextMatch();
            }

            private boolean findNextMatch() {
                // find next match only if both previous objects have been consumed
                if (nextForecast != null || (nextSnapshot != null && lastSnapShotsMatches)) return true;

                if (!cursor.hasNext()) return false;

                while (cursor.hasNext() && !lastSnapShotsMatches) {
                    nextForecast = cursor.next();
                    long time = ((Date)nextForecast.get("datetime")).getTime();

                    long curTime = 0;
                    if (!lastSnapShotsMatches && nextSnapshot != null) {
                        // there's a previously retrieved snapshot that didn't match last time
                        curTime = ((Date)nextSnapshot.get("datetime")).getTime();
                        if (curTime == time) lastSnapShotsMatches = true;
                    } else {
                        lastSnapShotsMatches = false;
                        nextSnapshot = null;
                    }


                    while (snapshots.hasNext() && curTime < time && !lastSnapShotsMatches) {
                        nextSnapshot = snapshots.next();
                        curTime = ((Date)nextSnapshot.get("datetime")).getTime();
                        if (curTime == time) lastSnapShotsMatches = true;
                    }

                }

                return lastSnapShotsMatches;
            }

            @Override
            public Collection next() {
                DBObject obj;

                if (!findNextMatch()) throw new IllegalStateException("No more records that match");

                if (nextForecast != null) {
                    obj = nextForecast;
                    nextForecast = null;
                }
                else if (lastSnapShotsMatches) {
                    obj = nextSnapshot;
                    nextSnapshot = null;
                    lastSnapShotsMatches = false;
                }
                else throw new IllegalStateException("Missing matching snapshot"); // should not reach this


                Bindings bindings = new DBObjectBindings(obj);
                //bindings.putAll(obj.toMap());
                bindings.put("dbobj", obj);

                List result = new ArrayList();
                //WARN even though cursor is the result of a query, mongo does not guarantee
                // queriedFields to be ordered --> fetch by field name
                if (queriedFields != null)
                    for (ColumnDescriptor key: queriedFields) {
                        Object value;
                        if (key instanceof FunctionalColumn) {
                            try {
                                value = compiledScripts.get(key.getName()).eval(bindings);
                            } catch (ScriptException e) {
                                log.warn("Failed to evaluate expression "+((FunctionalColumn)key).getExpression(), e);
                                value = null;
                            }
                        } else
                            value = obj.get(key.getName());
                        result.add(value);
                    }
                else {
                    log.warn("Doing mongo query with no column filter");
                    for (String key: obj.keySet()) result.add(obj.get(key));
                }
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
