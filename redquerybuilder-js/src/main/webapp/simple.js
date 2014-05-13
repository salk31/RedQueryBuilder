RedQueryBuilderFactory.create({
	meta : {
		tables : [ {
			"name" : "PERSON",
			"label" : "Person",
			"columns" : [ {
				"name" : "NAME",
				"label" : "Name",
				"type" : "STRING",
				"editor" : "SUGGEST",
				"size" : 10
			}, {
				"name" : "DOB",
				"label" : "Date of birth",
				"type" : "DATE"
			}, {
				"name" : "HEADING1",
				"label" : "*Heading 1*",
				"type" : "STRING"
			}, {
				"name" : "SEX",
				"label" : "Sex",
				"type" : "STRING",
				"editor" : "SELECT"
			}, {
				"name" : "HEADING2",
				"label" : "*Heading 2 goes on a bit*",
				"type" : "STRING"
			}, {
				"name" : "CATEGORY",
				"label" : "Category",
				"type" : "REF",
			}  ],
			fks : []
		} ],

		types : [ {
			"name" : "STRING",
			"editor" : "TEXT",
			"operators" : [ {
				"name" : "=",
				"label" : "is",
				"cardinality" : "ONE"
			}, {
				"name" : "<>",
				"label" : "is not",
				"cardinality" : "ONE"
			}, {
				"name" : "LIKE",
				"label" : "like",
				"cardinality" : "ONE"
			}, {
				"name" : "<",
				"label" : "less than",
				"cardinality" : "ONE"
			}, {
				"name" : ">",
				"label" : "greater than",
				"cardinality" : "ONE"
			} ]
		}, {
			"name" : "DATE",
			"editor" : "DATE",
			"operators" : [ {
				"name" : "=",
				"label" : "is",
				"cardinality" : "ONE"
			}, {
				"name" : "<>",
				"label" : "is not",
				"cardinality" : "ONE"
			}, {
				"name" : "<",
				"label" : "before",
				"cardinality" : "ONE"
			}, {
				"name" : ">",
				"label" : "after",
				"cardinality" : "ONE"
			} ]
		}, {
			"name" : "REF",
			"editor" : "CUSTOM",
			"operators" : [ {
				"name" : "IN",
				"label" : "any of",
				"cardinality" : "MULTI"
			}]
		}  ]
	},
	onSqlChange : function(sql, args) {
		var out = sql + '\r\n';
		for (var i = 0; i < args.length; i++) {
			var arg = args[i];
			out += 'arg' + i;
			if (arg != null) {
				out += ' type=' + Object.prototype.toString.call(arg) + ' toString=' + arg;
			} else {
				out += ' null';
			}
			out += '\r\n';
		}
		document.getElementById("debug").value = out;
	},
	enumerate : function(request, response) {
		if (request.columnName == 'CATEGORY') {
			response([{value:'A', label:'Small'}, {value:'B', label:'Medium'}]);
		} else {
			response([{value:'M', label:'Male'}, {value:'F', label:'Female'}]);
		}
	},
	suggest : function(request, response) {
		var result = [];
		for (var i = 0; i < request.limit; i++) {
			result.push("X" + (request.page * request.limit + i));
		}
		
		response(result, true);
	},
	editors : [ {
		name : 'DATE',
		format : 'dd.MM.yyyy'
	},
	{
		name : 'CUSTOM',
		create : function(elmt) {

			var obj = {
				getValue: function() {
					return this.input.value;
				},
				setValue: function(newValue) {
					this.input.value = newValue;
				},
				addValueChangeHandler: function(handler) {
					this.input.onchange = handler;
				}
			}
			elmt.innerText='hello';
			obj.input = document.createElement('input');
			obj.input.setAttribute('type', 'text');
			elmt.appendChild(obj.input);
			return obj;
		}
	}]
});