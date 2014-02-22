
package htm.core;

import htm.Input;
import htm.InputReceiver;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author david.charubini
 */
public class Column extends Input<Boolean> {

    private final DescriptiveStatistics activeDutyCycle = new DescriptiveStatistics(1000);
    private final Segment proximalDendrite;
    private final Collection<Cell> cells;
    
    private boolean suppressed = false;

    public Column(String id, Segment proximalDendrite, Collection<Cell> cells) {
        super(id);
        this.proximalDendrite = proximalDendrite;
        this.cells = cells;
    }
    
    public void getConnectedInputs(Collection<Input<?>> connectedInputs) {
        if (!this.suppressed) {
            this.proximalDendrite.getConnectedInputs(connectedInputs);
        }
    }

    @Override
    public String getDisplayString() {
        return "Column ["+ this.getId() + "] active [" + this.isActive() + "]";
    }
    
    public Collection<InputReceiver> getFeedForwardInputReceivers() {
        return this.proximalDendrite.getInputReceivers();
    }
    
    private Collection<Segment> getSegments(Collection<Column> columns) {
        Collection<Segment> localSegments = new ArrayList<Segment>();
        
        for (Column localColumn : columns) {
            if (!localColumn.isSuppressed()) {
                localSegments.add(localColumn.proximalDendrite);
            }
        }
        
        return localSegments;
    }
    
    @Override
    public boolean isActive() {
        return !this.suppressed && this.isFeedForwardActive();
//        return !this.suppressed && (this.isFeedForwardActive() || this.isHorizontalActive());
    }

    private boolean isFeedForwardActive() {
        return this.proximalDendrite.isActive();
    }
    
    private boolean isHorizontalActive() {
        
        for (Cell cell : this.cells) {
            if (cell.isActive()) {
                return true;
            }
        }
        
        return false;
    }

    public boolean isSuppressed() {
        return this.suppressed;
    }
    
    public void learn(Collection<Column> neighbors) {
        
        // TBD: There may be a column-specific active duty cycle which affects
        // the columns activity as well as the segment-version.  This is to
        // say that we may not want to pass the activeDutyCycle value into
        // the segment - it should be able to handle itsself.
        
        this.proximalDendrite.learn(this.isActive(), this.activeDutyCycle.getMean(), getSegments(neighbors));
    }
    
    public void process() {
        this.proximalDendrite.process();
        this.activeDutyCycle.addValue(this.isFeedForwardActive() ? 1.0 : 0.0);
    }
    
    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }
    
    public boolean isActivityGreaterThanLocal(Collection<Column> neighbors) {
        Collection<Segment> localSegments = getSegments(neighbors);
        return this.proximalDendrite.isOverlapGreaterThanLocal(localSegments);
    }
}
