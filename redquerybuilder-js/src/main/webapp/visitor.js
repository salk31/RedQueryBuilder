RedQueryBuilderFactory.create({
	targetId : 'rqbVisitor',
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
	onLoad : function(instance) {
		// XXX this really how you get it?
		this.instance = instance;
	},
	onSqlChange : function(sql, args) {
		var visitor = new rqb.Visitor();
		var debug = document.getElementById("rqbVisitorDebug");
		var indent = 0;
		debug.value = '';
		visitor.visit = function(ctx) {
			debug.value += "  ".repeat(indent);
			debug.value += ctx.getNodeType();
			if (ctx.getNodeName() != null) {
				debug.value += " " + ctx.getNodeName();
			}
			if (ctx.getNodeType() == 'PARAMETER') {
				var msg = new rqb.Message('Hello ' + ctx.getValue());
				ctx.showMessage(msg);
			}
			debug.value += "\n";
			indent++;
		}
		visitor.endVisit = function(ctx) {
			indent--;
		}
		this.instance.accept(visitor);
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