
{meta : {
        tables : [ {
            "name" : "ticket",
            "label" : "Ticket",
            "columns" : [ {
                "name" : "priority",
                "label" : "Priority",
                "type" : "REF"
            }  ],
            fks : []
        } ],

        types : [ {
            "name" : "REF",
            "editor" : "SELECT",
            "operators" : [ {
                "name" : "IN",
                "label" : "any of",
                "cardinality" : "MULTI"
            }]
        }  ]
    }
}
