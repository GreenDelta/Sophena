package sophena.model.biogas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

import sophena.model.AbstractEntity;
import sophena.model.CostSettings;
import sophena.model.Fuel;

/// The settings of a biogas plant. This is similar to the cost settings of a
/// project: a biogas plant is always created with a settings instance.
@Entity
@Table(name = "tbl_biogas_plant_settings")
public class BiogasPlantSettings extends AbstractEntity {

	/// Average hourly wage in EUR.
	///
	/// @de Mittlerer Stundenlohn
	@Column(name = "hourly_wage")
	public double hourlyWage;

	/// Price for purchased electricity in EUR/kWh when operating in full
	/// feed-in mode or when the CHP unit is idle.
	///
	/// @de Strompreis
	@Column(name = "electricity_price")
	public double electricityPrice;

	/// The electricity mix used for self-consumption when the biogas plant
	/// is idle or when operating in full feed-in mode.
	///
	/// @de Verbrauchter Strom
	@OneToOne
	@JoinColumn(name = "f_demand_electricity_mix")
	public Fuel demandElectricityMix;

	/// Indicates whether the plant operates in full feed-in mode. In full feed-in
	/// mode, the electricity price above is always used for self-consumption. In
	/// surplus feed-in mode, the self-consumption is subtracted from the installed
	/// capacity when the CHP is running and not fed into the grid; at other times,
	/// the electricity price above is used.
	///
	/// @de Überschusseinspeisung | Volleinspeisung
	@Column(name = "is_full_feed_in")
	public boolean isFullFeedIn;

	/// Selling price for manure-based biomethane in EUR/kWh.
	///
	/// @de Verkaufspreis Biomethan aus Wirtschaftsdünger
	@Column(name = "manure_biomethane_price")
	public double manureBiomethanePrice;

	/// Selling price for non-manure-based biomethane in EUR/kWh.
	///
	/// @de Verkaufspreis Biomethan aus Nicht-Wirtschaftsdünger
	@Column(name = "non_manure_biomethane_price")
	public double nonManureBiomethanePrice;

	/// Average internal electricity demand in kW.
	///
	/// @de Durchschnittlicher Eigenstrombedarf
	@Column(name = "avg_power_demand")
	public double avgPowerDemand;

	/// Cable and transformer losses in kW. Transformer losses are typically
	/// 0.7 % of the electricity production; cable losses depend on the voltage,
	/// the power and the cable length. At medium voltage, losses are hardly
	/// relevant.
	///
	/// @de Kabel- und Trafoverluste
	@Column(name = "transmission_losses")
	public double transmissionLosses;

	/// Methane slip in %. This is the share of methane that is not converted or
	/// captured, e.g. due to leaks in the gas hood or at the CHP unit.
	///
	/// @de Methanschlupf
	@Column(name = "methane_slip")
	public double methaneSlip;

	/// Heat losses in kW that may occur before feeding into the heat network.
	/// Heat losses within the heat network are accounted for elsewhere.
	///
	/// @de Wärmeverluste
	@Column(name = "heat_loss")
	public double heatLoss;

	/// Capital mixed interest rate in %.
	///
	/// @de Kapital-Mischzinssatz
	@Column(name = "interest_rate")
	public double interestRate;

	/// Indicates whether the expected profit is based on the expected rate of
	/// return (`rateOfReturn`) or on the expected annual surplus
	/// (`expectedAnnualSurplus`).
	///
	/// @de Renditeerwartung | Jahresüberschusserwartung
	@Column(name = "use_rate_of_return")
	public boolean useRateOfReturn;

	/// The expected rate of return in %. This is only used when
	/// `useRateOfReturn` is `true`.
	///
	/// @de Renditeerwartung
	@Column(name = "rate_of_return")
	public double rateOfReturn;

	/// The expected annual surplus in EUR/a. This is only used when
	/// `useRateOfReturn` is `false`.
	///
	/// @de Jahresüberschusserwartung
	@Column(name = "expected_annual_surplus")
	public double expectedAnnualSurplus;

	/// General investment funding in EUR. This field is not shown in the user
	/// interface yet. It is kept in the model and the database so that it can be
	/// used in later extensions without requiring a database change.
	///
	/// @de Investitionsförderung absolut
	@Column(name = "funding")
	public double funding;

	/// The maximum rated electrical power of the plant in kW. A `null` value
	/// means that no maximum rated power is defined.
	///
	/// @de Höchstbemessungsleistung
	@Column(name = "max_rated_power")
	public Double maxRatedPower;

	/// The number of operating quarter hours per year for which a claim to
	/// payment under §19 exists. Quarter hours are operating quarter hours when
	/// at least one boiler runs with any power.
	///
	/// @de Förderfähige Betriebsviertelstunden
	@Column(name = "eligible_quarter_hours")
	public int eligibleQuarterHours;

	/// Insurance costs as a percentage of the investment.
	///
	/// @de Versicherung
	@Column(name = "insurance_costs_share")
	public double insuranceCostsShare;

	/// Other charges as a percentage of the investment, e.g. taxes, lease
	/// payments, etc.
	///
	/// @de Sonstige Abgaben (Steuern, Pacht, usw.)
	@Column(name = "other_costs_share")
	public double otherCostsShare;

	/// Creates a new settings instance with the default values. The demand
	/// electricity mix is taken from the given global cost settings, if it is
	/// available.
	public static BiogasPlantSettings createDefault(CostSettings global) {
		var settings = new BiogasPlantSettings();
		settings.id = UUID.randomUUID().toString();
		settings.hourlyWage = 25.0;
		settings.electricityPrice = 0.30;
		settings.isFullFeedIn = false;
		settings.manureBiomethanePrice = 0.12;
		settings.nonManureBiomethanePrice = 0.08;
		settings.avgPowerDemand = 30.0;
		settings.transmissionLosses = 7.35;
		settings.methaneSlip = 1.8;
		settings.heatLoss = 0.0;
		settings.interestRate = 4.0;
		settings.useRateOfReturn = true;
		settings.rateOfReturn = 20.0;
		settings.expectedAnnualSurplus = 0.0;
		settings.funding = 0.0;
		settings.eligibleQuarterHours = 35040;
		settings.insuranceCostsShare = 0.50;
		settings.otherCostsShare = 0.25;
		if (global != null) {
			settings.demandElectricityMix = global.electricityMix;
		}
		return settings;
	}

	@Override
	public BiogasPlantSettings copy() {
		var copy = new BiogasPlantSettings();
		copy.id = UUID.randomUUID().toString();
		copy.hourlyWage = hourlyWage;
		copy.electricityPrice = electricityPrice;
		copy.demandElectricityMix = demandElectricityMix;
		copy.isFullFeedIn = isFullFeedIn;
		copy.manureBiomethanePrice = manureBiomethanePrice;
		copy.nonManureBiomethanePrice = nonManureBiomethanePrice;
		copy.avgPowerDemand = avgPowerDemand;
		copy.transmissionLosses = transmissionLosses;
		copy.methaneSlip = methaneSlip;
		copy.heatLoss = heatLoss;
		copy.interestRate = interestRate;
		copy.useRateOfReturn = useRateOfReturn;
		copy.rateOfReturn = rateOfReturn;
		copy.expectedAnnualSurplus = expectedAnnualSurplus;
		copy.funding = funding;
		copy.maxRatedPower = maxRatedPower;
		copy.eligibleQuarterHours = eligibleQuarterHours;
		copy.insuranceCostsShare = insuranceCostsShare;
		copy.otherCostsShare = otherCostsShare;
		return copy;
	}
}
