
package htm.core;

import htm.Input;
import htm.InputReceiver;
import htm.InputSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author david.charubini
 */
public class Segment {
    
    private static final double ACTIVE_THRESHOLD    = 0.1;
    private static final double MIN_OVERLAP_RATIO   = 0.1;
    private static final double BOOST               = 0.01;

    private final DescriptiveStatistics overlapDutyCycle = new DescriptiveStatistics(1000);
    private final Collection<Synapse> synapses;
    
    private double overlapRatio = 0.0;
    
    // Having the boost here is a bit sketchy - this sort of pushes this into 
    // the realm of being a proximal dendrite segment.
    private double boost = new Random().nextDouble();
    
    private boolean isActive = false;
    
    public Segment(Collection<Synapse> synapses) {
        
        if (synapses.isEmpty()) {
            throw new IllegalArgumentException("must have at least 1 synapse");
        }
        
        this.synapses = Collections.unmodifiableCollection(synapses);
    }
    
    private double getBoost(double activeDutyCycle, double minDutyCycle) {
        if (activeDutyCycle >= minDutyCycle) {
            return 1.0;
        } else {
            return this.boost + BOOST;
        }
    }
    
    /**
     * Returns the average connection rate of this segment.
     * 
     * @return the average connection rate of this segment.
     */
    public double getOverlapDutyCycle() {
        return this.overlapDutyCycle.getMean() * 100.0;
    }
    
    public void getConnectedInputs(Collection<Input<?>> connectedInputs) {
        for (Synapse syn : this.synapses) {
            if (syn.isConnected()) {
                Input<?> input = syn.getInput();
                connectedInputs.add(input);
            }
        }
    }
    
    public Collection<InputReceiver> getInputReceivers() {
        Collection<InputReceiver> inputReceivers = new ArrayList<InputReceiver>();
        
        for (Synapse syn : this.synapses) {
            inputReceivers.add(syn);
        }
        
        return inputReceivers;
    }
    
    public boolean isActive() {
        return this.isActive;
    }
    
    boolean isOverlapGreaterThanLocal(Collection<Segment> localSegments) {
        
        int equalCount = 0;
        
        for (Segment segment : localSegments) {
            if (this.overlapRatio > 0) {
                if (this.overlapRatio > segment.overlapRatio) {
                    return true;
                } else if (this.overlapRatio == segment.overlapRatio) {
                    equalCount ++;
                }
            }
        }
        
        return equalCount == localSegments.size();
    }
    
    public void process() {
        
        int connectedCount = 0;
        
        for (Synapse synapse : this.synapses) {
            if (synapse.isConnected() && synapse.isActive()) {
                connectedCount ++;
            }
        }
        
        double overlap = (double) connectedCount / this.synapses.size();
        
        if (overlap >= MIN_OVERLAP_RATIO) {
            this.overlapRatio = overlap * this.boost;
        } else {
            this.overlapRatio = 0.0;
        }
        
        this.overlapDutyCycle.addValue(this.overlapRatio);
        
        this.isActive = this.overlapRatio >= ACTIVE_THRESHOLD;
    }
    
    public void learn(boolean isActive, double activeDutyCycle, Collection<Segment> localSegments) {
        if (isActive) {
            for (Synapse synapse : this.synapses) {
                synapse.adjustPermanence();
            }
        }
        
        // calculate max duty cycle
        double maxDutyCycle = 0.0;
        
        for (Segment local : localSegments) {
            if (local.overlapRatio > maxDutyCycle) {
                maxDutyCycle = local.overlapRatio;
            }
        }
        
        // calculate the mix duty cycle
        double minDutyCycle = 0.01 * maxDutyCycle;
        this.boost = getBoost (activeDutyCycle, minDutyCycle);
        
        
        // TODO: check the overlapdutycycle and increase synapses
    }

    void setInput(InputSet inputSet) {
        for (Synapse synapse : this.synapses) {
            String id = synapse.getId();
            Input<?> input = inputSet.getById(id);
            synapse.send(input);
        }
    }
}