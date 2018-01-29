
def text = """
    @Enumerated(EnumType.STRING)
	@Column(name = "product_type")
	public ProductType type;

	@Column(name = "idx")
	public int index;

	/** Default usage duration of this product group given in years. */
	@Column(name = "duration")
	public int duration;

	/** Default fraction [%] of the investment that is used for repair. */
	@Column(name = "repair")
	public double repair;

	/** Default fraction [%] of the investment that is used for maintenance . */
	@Column(name = "maintenance")
	public double maintenance;

	/** Default amount of hours that are used for operation in one year. */
	@Column(name = "operation")
	public double operation;
"""

text.eachLine {
    def parts = it.trim().split(' ')
    if (parts[0] != 'public')
        return
    def jType = parts[1]
    def jField = parts[2].substring(0, parts[2].length()-1)
    def gField = jField[0].toUpperCase() +jField.substring(1)
    
    def gType = '<?>'+jType
    def opt = ''
    if (jType == 'double') {
        gType = 'float64'   
    }
    if (jType == 'Double') {
        gType = '*float64'
        opt = ',omitempty'
    } 
    if (jType == 'boolean') {
        gType = 'bool'   
    } 
    if (jType == 'String') {
        gType = 'string'
        opt = ',omitempty'
    }
    if (jType == 'int') {
        gType = 'int'
    }
        
    
    println """ $gField $gType `json:"$jField$opt"` """  
}
