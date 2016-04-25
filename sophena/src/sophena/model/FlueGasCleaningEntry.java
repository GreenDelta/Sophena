package sophena.model;

@Embeddable
public class FlueGasCleaningEntry {
    
    @OneToOne
    @JoinColumn(name = "f_flue_gas_cleaning")
    public FlueGasCleaning product;
    
    @Embedded
    public ProductCosts costs;
    
    @Override
    public FlueGasCleaningEntry clone() {
        FlueGasCleaningEntry clone = new FlueGasCleaningEntry();
        clone.product = product;
        if (costs != null) {
            clone.costs = costs.clone();
        }
        return clone;
    }    
}