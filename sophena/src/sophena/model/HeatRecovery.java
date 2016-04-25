package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_heat_recovery")
public class HeatRecovery extends AbstractProduct {

    @Column(name = "power")
    public double power;
    
    @Column(name = "heat_recovery_type")
    public String heatRecoveryType;
    
    @OneToOne
    @JoinColumn(name = "f_fuel")
    public Fuel fuel;
    
    @Column(name = "producer_power")
    public double producerPower;
    
}