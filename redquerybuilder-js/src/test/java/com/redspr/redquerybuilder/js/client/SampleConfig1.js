
{meta : {
        tables : [ {
            "name" : "ticket",
            "label" : "Ticket",
            "columns" : [ {
                "name" : "priority",
                "label" : "Priority",
                "type" : "REF"
            },
            {
                "name" : "owner_id",
                "label" : "Owner",
                "type" : "TEXT"
            }],
            fks : []
        },
        {
            "name" : "user",
            "label" : "User",
            "columns" : [ {
                "name" : "id",
                "label" : "id",
                "type" : "TEXT"
            }  ],
            fks : []
        }],

        types : [ {
            "name" : "REF",
            "editor" : "SELECT",
            "operators" : [ {
                "name" : "IN",
                "label" : "any of",
                "cardinality" : "MULTI"}]
            },
            {
                "name" : "TEXT",
                "editor" : "TEXT",
                "operators" : [ {
                    "name" : "=",
                    "label" : "="
                }]
            
        }  ]
    }
}
