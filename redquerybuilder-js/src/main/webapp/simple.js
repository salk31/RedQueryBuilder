RedQueryBuilderFactory.create({
	meta : {
		tables : [ {
			"name" : "PERSON",
			"label" : "Person",
			"columns" : [ {
				"name" : "NAME",
				"label" : "Name",
				"type" : "STRING",
				"size" : 10
			}, {
				"name" : "DOB",
				"label" : "Date of birth",
				"type" : "DATE"
			}, {
				"name" : "SEX",
				"label" : "Sex",
				"type" : "STRING",
				"editor" : "SELECT"
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
			"editor" : "SELECT",
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
	editors : [ {
		name : 'DATE',
		format : 'dd.MM.yyyy'
	} ]
});