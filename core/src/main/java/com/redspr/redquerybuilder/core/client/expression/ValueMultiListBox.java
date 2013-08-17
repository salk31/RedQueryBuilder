package com.redspr.redquerybuilder.core.client.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SimpleKeyProvider;

public class ValueMultiListBox<T> extends Composite implements
        HasValue<Collection<T>>, IsEditor<TakesValueEditor<Collection<T>>> {

    private final List<T> values = new ArrayList<T>();
    private final Map<Object, Integer> valueKeyToIndex = new HashMap<Object, Integer>();
    private final Renderer<T> renderer;
    private final ProvidesKey<T> keyProvider;

    private TakesValueEditor<Collection<T>> editor;
    private Collection<T> value;

    public ValueMultiListBox(Renderer<T> renderer2) {
        this(renderer2, new SimpleKeyProvider<T>());
    }

    public ValueMultiListBox(Renderer<T> renderer2, ProvidesKey<T> keyProvider2) {
        this.keyProvider = keyProvider2;
        this.renderer = renderer2;
        initWidget(new ListBox(true));
        getListBox().setVisibleItemCount(2);

        getListBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                ListBox lb = getListBox();
                Collection<T> newValue = new ArrayList<T>();
                for (int i = 0; i < values.size(); i++) {
                    if (lb.isItemSelected(i)) {
                        newValue.add(values.get(i));
                    }
                }

                setValue(newValue, true);
            }
        });
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Collection<T>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Returns a {@link TakesValueEditor} backed by the ValueListBox.
     */
    @Override
    public TakesValueEditor<Collection<T>> asEditor() {
        if (editor == null) {
            editor = TakesValueEditor.of(this);
        }
        return editor;
    }

    @Override
    public Collection<T> getValue() {
        return value;
    }

    public void setAcceptableValues(Collection<T> newValues) {
        values.clear();
        valueKeyToIndex.clear();
        ListBox listBox = getListBox();
        listBox.clear();

        for (T nextNewValue : newValues) {
            addValue(nextNewValue);
        }

        updateListBox();
    }

    /**
     * Set the value and display it in the select element. Add the value to the
     * acceptable set if it is not already there.
     */
    @Override
    public void setValue(Collection<T> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Collection<T> value, boolean fireEvents) {
        if (value == this.value
                || (this.value != null && this.value.equals(value))) {
            return;
        }

        Collection<T> before = this.value;
        this.value = value;
        updateListBox();

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }

    private void addValue(T value) {
        Object key = keyProvider.getKey(value);
        if (valueKeyToIndex.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate value: " + value);
        }

        valueKeyToIndex.put(key, values.size());
        values.add(value);
        getListBox().addItem(renderer.render(value));
        assert values.size() == getListBox().getItemCount();
    }

    private ListBox getListBox() {
        return (ListBox) getWidget();
    }

    private void updateListBox() {
        for (T item : values) {
            Object key = keyProvider.getKey(item);
            Integer index = valueKeyToIndex.get(key);
            if (index == null) {
                addValue(item);
            }
        }

        for (int i = 0; i < values.size(); i++) {
            getListBox().setItemSelected(i, value != null && value.contains(values.get(i)));
        }
    }
}
