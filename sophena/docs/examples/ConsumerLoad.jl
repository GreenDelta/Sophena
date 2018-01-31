Q_total = 1200  # kWh, total used heat
wf = 0.1        # 10%, water fraction
T_lim = 10      # °C

# temperature data
T = [-8., -8., -7., -7., -6., -4., -2.,
      0., 2., 4., 6., 7., 8., 8., 8.,
      8., 7., 6., 6., 5., 4., 2., 1., 1. ]

# interruptions
T = [-8., -8., -7., -7., -6., -4., -2.,
      0., 2., 4., 6., 7., 8., 8., 8.,
      8., 7., 6., 6., 5., 4., 2., 1., 1. ]

# calculate static load
Q_water = wf * Q_total          # kWh
P_stat = zeros(length(T))
for i = 1:length(T)
    P_stat[i] = Q_water / length(T)  # kW
end

# calculate heating degrees
T_lim = 10.0
hd = zeros(length(T))
for i = 1:length(T)
    if T[i] < T_lim
        hd[i] = T_lim - T[i]
    end
end

# calulcate dynamic & total load
Q_hd = (Q_total - Q_water) / sum(hd)
P_dyn = zeros(length(T))
P = zeros(length(T))
for i = 1:length(T)
    P_dyn[i] = hd[i] * Q_hd
    P[i] = P_stat[i] + P_dyn[i]
end

using Gadfly
Gadfly.push_theme(Theme(panel_fill="white"))
plot(
    layer(x = 1:24, y = P_stat, Geom.line,
        Theme(default_color=color("blue"))),
    layer(x = 1:24, y = P_dyn, Geom.line,
        Theme(default_color=color("orange"))),
    layer(x = 1:24, y = P, Geom.line,
        Theme(default_color=color("red"))))
