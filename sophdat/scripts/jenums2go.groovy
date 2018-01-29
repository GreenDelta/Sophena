
def text = """
    BIOMASS_BOILER,

	FOSSIL_FUEL_BOILER,

	COGENERATION_PLANT,

	BOILER_ACCESSORIES,

	BUFFER_TANK,

	HEAT_RECOVERY,

	FLUE_GAS_CLEANING,

	BOILER_HOUSE_TECHNOLOGY,

	BUILDING,

	HEATING_NET_TECHNOLOGY,

	HEATING_NET_CONSTRUCTION,

	PLANNING,

	TRANSFER_STATION
"""

def enumName = 'ProductType'
def prefix = 'Product'

text.eachLine {
    def c = it.trim()
    if (c == '')
        return
    if (c[c.length()-1] == ',')
        c = c.substring(0, c.length() - 1)
    
    def gC = prefix
    def parts = c.split('_')
    for (part in parts) {
        gC += part[0] + part.substring(1, part.length()).toLowerCase()
    }    
    println """    $gC $enumName = "$c" """
    
}