
package htm.core;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author david.charubini
 */
public class Column extends AbstractScheduledService {

    private static final Scheduler SCHEDULER = Scheduler.newFixedDelaySchedule(0, 500, TimeUnit.MILLISECONDS);
    
    private final String id;
    private final ProximalSegment proximalDendrite;
    private final Collection<Cell> cells;
    
    private Collection<Column> neighbors;

    public Column(String id, ProximalSegment proximalDendrite, Collection<Cell> cells) {
        super();
        this.id = id;
        this.proximalDendrite = proximalDendrite;
        this.cells = cells;
    }

    public String getDisplayString() {
        return "Column [" + this.id + "] active [" + this.isActive() + "]";
    }
    
    /*pkg*/ Collection<Segment> getNeighboringSegments() {
        Collection<Segment> localSegments = Lists.newArrayList();
        
        for (Column localColumn : this.neighbors) {
            localSegments.add(localColumn.proximalDendrite);
            
            // TBD: add distral dendrites?
        }
        
        return localSegments;
    }

    public Segment getProximalDendrite() {
        return this.proximalDendrite;
    }
    
    public boolean isActive() {
        return this.isFeedForwardActive() || this.isHorizontalActive();
    }

    private boolean isFeedForwardActive() {
        return this.proximalDendrite.isActive();
    }
    
    // TODO:
    private boolean isHorizontalActive() {
        
//        for (Cell cell : this.cells) {
//            if (cell.isActive()) {
//                return true;
//            }
//        }
        
        return false;
    }
    
    protected void doSpatialPooling() {
        // 1) Tell the lower level synapses to compute their overlap score
        this.proximalDendrite.process();
        
        // 3) learn
        this.proximalDendrite.learn(this.isActive(), getNeighboringSegments());
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        this.doSpatialPooling();
    }

    @Override
    protected Scheduler scheduler() {
        return SCHEDULER;
    }
    
    /*pkg*/ void setNeighbors (Collection<Column> neighbors) {
        if (neighbors == null) {
            throw new IllegalArgumentException();
        }
        
        // This set is not expected to happen more than once at init time.
        if (this.neighbors != null) {
            throw new IllegalStateException();
        }
        
        this.neighbors = neighbors;
    }
}
