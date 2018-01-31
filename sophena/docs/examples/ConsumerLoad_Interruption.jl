Q_total = 1200  # kWh, total used heat
wf = 0.1        # 10%, water fraction
T_lim = 10      # °C

# temperature data
T = [-8., -8., -7., -7., -6., -4., -2.,
      0., 2., 4., 6., 7., 8., 8., 8.,
      8., 7., 6., 6., 5., 4., 2., 1., 1. ]

# interruptions
Intr = zeros(Bool, 24)
for i = 2:6
    Intr[i] = true
end

# calculate static load
hours = 0
for i = 1:length(Intr)
    if !Intr[i]
        hours += 1
    end
end
Q_water = wf * Q_total          # kWh
P_stat = zeros(length(T))
for i = 1:length(T)
    if Intr[i]
        P_stat[i] = 0
    else
        P_stat[i] = Q_water / hours  # kW
    end
end

# calculate heating degrees
T_lim = 10.0
T_afr = 2.0
hd = zeros(length(T))
for i = 1:length(T)
    lim = T_lim
    if Intr[i]
        lim = T_afr
    end
    if T[i] < lim
        hd[i] = lim - T[i]
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
p = plot(
    layer(x = 1:24, y = P_stat, Geom.line,
        Theme(default_color=color("blue"))),
    layer(x = 1:24, y = P_dyn, Geom.line,
        Theme(default_color=color("orange"))),
    layer(x = 1:24, y = P, Geom.line,
        Theme(default_color=color("red"))))
draw(SVG("interr.svg"), p)
