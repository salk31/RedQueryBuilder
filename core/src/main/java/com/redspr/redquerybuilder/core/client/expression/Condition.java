package com.redspr.redquerybuilder.core.client.expression;

import com.redspr.redquerybuilder.core.client.engine.Session;



/**
 * Represents a condition returning a boolean value, or NULL.
 */
public abstract class Condition extends Expression {

  protected Condition(Session session) {
      super(session);
  }



}
