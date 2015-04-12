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
		visitor.visit = function(ctx) {
			if (ctx.asHasMessages()) {
				var msg = new rqb.Message('Hello ' + ctx.asHasValue().getValue());
				ctx.asHasMessages().showMessage(msg);
			}
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