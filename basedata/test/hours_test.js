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
});