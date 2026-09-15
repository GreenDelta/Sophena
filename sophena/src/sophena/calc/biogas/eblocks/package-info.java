/// A block based calculation of the runtime of a biogas plant.
///
/// The old calculation (`sophena.calc.biogas.ElectricityPriceSchedule`) tags
/// single hours in which the plant should run and only looks at the price of
/// that hour. A biogas plant has a minimum runtime, so the plant must run in
/// blocks of hours. This package implements a search that always searches and
/// extends whole blocks of hours.
///
/// ## Hours
///
/// The year has the hours `0` to `8759` (`sophena.model.Stats.HOURS`). An hour
/// `h` is the interval `[h, h + 1)` and a window or a block always covers a
/// half-open interval `[from, to)` which contains the hours `from` to
/// `to - 1`. Hour indices never wrap around the year.
///
/// ## The window of a block
///
/// A search step starts with a `State` at an hour `t_0`. From that state, the
/// storage is filled with the produced gas until it is full at `t_f`. If the
/// plant would then run under full load without interruption, the storage
/// would be empty at `t_e`:
///
/// ```
///   fill the storage     running the plant under full load
///  |-------------------|###################################|
///  t_0                 t_f                                 t_e
/// ```
///
/// The plant must start a block in `[t_0, t_f]` because the gas that is
/// produced in the hours after `t_f` does not fit into the storage anymore.
/// The run of the plant covers the hours `[t_f, t_e)`, so `n = t_e - t_f` is
/// the maximum number of hours that the plant can run. `n` is used to mark the
/// price optimal hours of the window.
///
/// ## Ramps
///
/// Starting and stopping a plant consumes additional fuel: the first hour of a
/// block needs `1.125` hours of fuel under full load (the ramp-up) and after
/// the last hour of a block another `0.125` hours are needed for the ramp-down
/// (`sophena.calc.biogas.Demand`). `Window` reserves the fuel for the ramp-down
/// when it computes `n`, so a block of `n` hours always fits into the window.
/// Blocks that directly follow each other run without a ramp-down and a
/// ramp-up in between and are merged into one block (`EblockSearch`).
///
/// ## Prices
///
/// The block price is the sum of the hourly prices (`Prices`). This is the
/// correct measure because the search first only compares blocks of the same
/// size, the minimum runtime of the plant. Hours in which feed-in is not
/// allowed are not removed from the calculation but get a high penalty
/// (`Prices.BLOCKED_FEED_IN_PENALTY`), so they are simply never attractive.
///
/// ## Errors
///
/// Every step of the search returns an `org.openlca.commons.Res`: if it runs
/// into a state that it cannot handle, the search stops with a message instead
/// of returning a wrong result. `PreCheck` rejects the plants for which the
/// search cannot work, so an error of a search step is always a sign of a
/// plant configuration that the search cannot calculate.
package sophena.calc.biogas.eblocks;
