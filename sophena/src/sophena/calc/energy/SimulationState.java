package sophena.calc.energy;

import java.util.HashMap;
import java.util.Map;

import sophena.model.Producer;
import sophena.model.Project;

/**
 * Aggregates all mutable state of an energy simulation run:
 * the buffer state, per-producer solar collector states,
 * per-producer heat pump states, and the solar calculation log.
 *
 * Once extracted from EnergyCalculator, it will be used as a single
 * state object whose methods replace the inline iteration loops.
 */
class SimulationState {

    final SolarLog solarLog;
    final BufferState bufferState;
    final Map<Producer, SolarState> solarStates;
    final Map<Producer, HeatPumpState> heatPumpStates;

    SimulationState(Project project) {
        solarLog = new SolarLog();
        bufferState = new BufferState(project, solarLog);
        solarStates = new HashMap<>();
        heatPumpStates = new HashMap<>();
    }

    // extracted in: SimulationState.initProducerStates()
    void initProducerStates(Project project, Producer[] producers) {
        // for (var producer : producers) {
        //   if (producer.solarCollector != null & producer.solarCollectorSpec != null)
        //     solarStates.put(producer, new SolarState(solarLog, project, producer));
        //   if (producer.heatPump != null)
        //     heatPumpStates.put(producer, new HeatPumpState(solarLog, project, producer));
        // }
    }

    // extracted in: SimulationState.calcPre()
    void calcPre(int hour) {
        // for (var state : solarStates.values()) {
        //   state.calcPre(hour, bufferState.getTE(), bufferState.getTV());
        // }
        // for (var state : heatPumpStates.values()) {
        //   state.calcPre(hour, bufferState.getTR(), bufferState.getTV());
        // }
    }

    // extracted in: SimulationState.calcPost()
    void calcPost(int hour) {
        // for (var state : solarStates.values()) {
        //   state.calcPost(hour);
        // }
        // for (var state : heatPumpStates.values()) {
        //   state.calcPost(hour);
        // }
    }

    // extracted in: SimulationState.collectResults()
    void collectResults(EnergyResult r) {
        // for (int k = 0; k < r.producers.length; k++) {
        //   var producer = r.producers[k];
        //   var solarState = solarStates.get(producer);
        //   if (solarState != null)
        //     r.producerStagnationDays[k] = solarState.numStagnationDays;
        //   var hpState = heatPumpStates.get(producer);
        //   if (hpState != null)
        //     r.producerJaz[k] = hpState.getJAZ();
        // }
    }

    // extracted in: SimulationState.hasAtLeastOneHTProducer()
    // Note: also needs isInterrupted() and isDisabledByOutdoorTemp()
    // (currently on EnergyCalculator) to be moved or passed in.
    boolean hasAtLeastOneHTProducer(int hour, boolean[][] interruptions, double TL_i, Producer[] producers) {
        // boolean hasHT = false;
        // for (int k = 0; k < producers.length; k++) {
        //   var producer = producers[k];
        //   var solarState = solarStates.get(producer);
        //   var hpState = heatPumpStates.get(producer);
        //   boolean isSolarProducer = solarState != null;
        //   var bufferLoadType = getBufferLoadType(producer, hour);
        //   if (bufferLoadType == BufferLoadType.NONE) continue;
        //   if (isSolarProducer && solarState.isNotOperating()) continue;
        //   if (isInterrupted(k, hour, interruptions)) continue;
        //   if (isDisabledByOutdoorTemp(producer, TL_i)) continue;
        //   if (bufferLoadType == BufferLoadType.HIGH_TEMP) hasHT = true;
        // }
        // return hasHT;
        return false;
    }

    // extracted in: SimulationState.getBufferLoadType()
    // Corresponds to EnergyCalculator.getProducerBufferLoadType()
    BufferLoadType getBufferLoadType(Producer producer, int hour) {
        // if (producer.profile != null && producer.profile.temperaturLevel != null) {
        //   double temp = producer.profile.temperaturLevel[hour];
        //   if (temp >= bufferState.getTMAX()) return BufferLoadType.HIGH_TEMP;
        //   if (temp >= bufferState.getTV())   return BufferLoadType.FLOW_TEMP;
        //   if (temp >= bufferState.getTR())   return BufferLoadType.LOW_TEMP;
        //   return BufferLoadType.NONE;
        // }
        // if (producer.productGroup == null) return BufferLoadType.HIGH_TEMP;
        // return switch (producer.productGroup.type) {
        //   case HEAT_PUMP -> {
        //     var hp = heatPumpStates.get(producer);
        //     yield hp != null ? hp.getBufferLoadType() : BufferLoadType.NONE;
        //   }
        //   case SOLAR_THERMAL_PLANT -> {
        //     var s = solarStates.get(producer);
        //     yield s != null ? s.getBufferLoadType() : BufferLoadType.NONE;
        //   }
        //   default -> BufferLoadType.HIGH_TEMP;
        // };
        return BufferLoadType.NONE;
    }

    // extracted in: SimulationState.getSuppliedPower()
    // Corresponds to EnergyCalculator.getSuppliedPower()
    double getSuppliedPower(Producer producer, int hour, double requiredLoad, double maxLoad) {
        // double bMin = Util.minPower(producer, hour);
        // double bMax = Util.maxPower(producer, solarStates.get(producer), heatPumpStates.get(producer), hour);
        // double load = producer.function == ProducerFunction.PEAK_LOAD
        //   ? requiredLoad
        //   : maxLoad;
        // return Math.min(Math.max(load, bMin), bMax);
        return 0;
    }

    // TODO (future): extract main producer loop (lines 103-201) as one or more
    // methods. The loop is complex and mixes state lookups, eligibility checks,
    // power calculation, and buffer load/unload logic, so a piecewise
    // extraction will be needed.
}
