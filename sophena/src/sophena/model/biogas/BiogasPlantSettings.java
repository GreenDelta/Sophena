package sophena.model.biogas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openlca.commons.Strings;

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

	// price change factors

	/// Price change factor for investments.
	///
	/// @de Investitionen
	@Column(name = "investment_factor")
	public double investmentFactor;

	/// Price change factor for biomass fuels.
	///
	/// @de Biomasse-Brennstoff
	@Column(name = "bio_fuel_factor")
	public double bioFuelFactor;

	/// Price change factor for fossil fuels.
	///
	/// @de Fossiler Brennstoff
	@Column(name = "fossil_fuel_factor")
	public double fossilFuelFactor;

	/// Price change factor for electricity.
	///
	/// @de Strom
	@Column(name = "electricity_factor")
	public double electricityFactor;

	/// Price change factor for wages/operation.
	///
	/// @de Lohnkosten und sonstige Kosten
	@Column(name = "operation_factor")
	public double operationFactor;

	/// Price change factor for maintenance.
	///
	/// @de Instandhaltung
	@Column(name = "maintenance_factor")
	public double maintenanceFactor;

	/// Price change factor for electricity revenues.
	///
	/// @de Strommehrerlöse
	@Column(name = "electricity_revenues_factor")
	public double electricityRevenuesFactor;

	// funding

	/// Annual funding in EUR/a that is independent of the fed-in electricity,
	/// e.g. the flexibility premium and the flexibility surcharge.
	///
	/// @de Jährliche Förderungen
	@Column(name = "annual_funding")
	public double annualFunding;

	/// Indicates whether the electricity is sold with a fixed feed-in tariff
	/// (`true`) or via the market premium model (`false`).
	///
	/// @de Art der Stromvermarktung: Festvergütung | Marktprämienmodell
	@Column(name = "is_fixed_remuneration")
	public boolean isFixedRemuneration;

	/// The feed-in tariff in ct/kWh. This is only used in the fixed
	/// remuneration mode.
	///
	/// @de Einspeisevergütung
	@Column(name = "feed_in_tariff")
	public double feedInTariff;

	/// The value to be applied in ct/kWh (anzulegender Wert). This is only used
	/// in the market premium model.
	///
	/// @de Anzulegender Wert
	@Column(name = "market_premium_value")
	public double marketPremiumValue;

	/// The electricity market values that are used in the market premium
	/// model.
	///
	/// @de Marktwert
	@OneToOne
	@JoinColumn(name = "f_market_value")
	public ElectricityMarketValue marketValue;

	/// Indicates whether the annual value (`true`) or the monthly values
	/// (`false`) of the selected market value are used in the calculation.
	///
	/// @de Jahreswert | Monatswert
	@Column(name = "use_annual_market_value")
	public boolean useAnnualMarketValue;

	/// The share of the direct marketer in % of the additional revenues.
	///
	/// @de Direktvermarkteranteil
	@Column(name = "direct_marketer_share")
	public double directMarketerShare;

	/// The electricity price limit below which no market premium is paid.
	///
	/// @de Keine Marktprämie bei Strompreisen
	@Column(name = "market_price_limit")
	@Enumerated(EnumType.STRING)
	public MarketPriceLimit marketPriceLimit;

	/// Creates a new settings instance with the default values. The demand
	/// electricity mix is taken from the given global cost settings, if it is
	/// available.
	public static BiogasPlantSettings createDefault(CostSettings global) {
		return createDefault(global, null);
	}

	/// Creates a new settings instance with the default values. The demand
	/// electricity mix is taken from the given global cost settings and the
	/// market value from the given list of market values, if they are
	/// available. The market value that comes first when the list is sorted by
	/// name in descending order is selected. When the list is empty, the fixed
	/// remuneration mode is used.
	public static BiogasPlantSettings createDefault(
		CostSettings global, List<ElectricityMarketValue> marketValues
	) {
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
		settings.investmentFactor = 1.02;
		settings.bioFuelFactor = 1.02;
		settings.fossilFuelFactor = 1.03;
		settings.electricityFactor = 1.03;
		settings.operationFactor = 1.02;
		settings.maintenanceFactor = 1.02;
		settings.electricityRevenuesFactor = 1.0;
		settings.annualFunding = 0.0;
		settings.feedInTariff = 0.0;
		settings.marketPremiumValue = 18.0;
		settings.useAnnualMarketValue = true;
		settings.directMarketerShare = 0.0;
		settings.marketPriceLimit = MarketPriceLimit.NONE;
		settings.isFixedRemuneration = true;
		if (global != null) {
			settings.demandElectricityMix = global.electricityMix;
		}
		initMarketValue(settings, marketValues);
		return settings;
	}

	/// Selects the market value that comes first when the given values are
	/// sorted by name in descending order and activates the market premium
	/// model. When there is no market value, the fixed remuneration mode is
	/// used.
	private static void initMarketValue(
		BiogasPlantSettings settings, List<ElectricityMarketValue> values
	) {
		var markets = values == null
			? new ArrayList<ElectricityMarketValue>()
			: new ArrayList<>(values);
		markets.sort((a, b) -> Strings.compareIgnoreCase(b.name, a.name));
		if (markets.isEmpty())
			return;
		settings.isFixedRemuneration = false;
		settings.marketValue = markets.getFirst();
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
		copy.investmentFactor = investmentFactor;
		copy.bioFuelFactor = bioFuelFactor;
		copy.fossilFuelFactor = fossilFuelFactor;
		copy.electricityFactor = electricityFactor;
		copy.operationFactor = operationFactor;
		copy.maintenanceFactor = maintenanceFactor;
		copy.electricityRevenuesFactor = electricityRevenuesFactor;
		copy.annualFunding = annualFunding;
		copy.isFixedRemuneration = isFixedRemuneration;
		copy.feedInTariff = feedInTariff;
		copy.marketPremiumValue = marketPremiumValue;
		copy.marketValue = marketValue;
		copy.useAnnualMarketValue = useAnnualMarketValue;
		copy.directMarketerShare = directMarketerShare;
		copy.marketPriceLimit = marketPriceLimit;
		return copy;
	}
}
