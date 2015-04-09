var hours = require('../lib/hours.js'),
	expect = require('chai').expect;


describe("hours.Index", function() {

	var idx = new hours.Index();

	describe("#getKey()", function() {	

		it("should return '010100' for hour=1", function() {			
			expect(idx.getKey(1)).to.equal('010100');
		});

		it("should return '123123' for hour=8760", function() {
			expect(idx.getKey(8760)).to.equal('123123');
		});
	});

	describe("#getHour()", function() {
		it("should return 1 for key=010100", function() {
			expect(idx.getHour('010100')).to.equal(1);
		});

		it("should return 8760 for key=123123", function(){
			expect(idx.getHour('123123')).to.equal(8760);
		});
	});

});