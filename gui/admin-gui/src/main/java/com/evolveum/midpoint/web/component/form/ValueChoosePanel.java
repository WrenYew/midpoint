/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.web.component.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.component.ObjectBrowserPanel;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.query.InOidFilter;
import com.evolveum.midpoint.prism.query.NotFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.page.admin.dto.ObjectViewDto;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 *
 * TODO: rename to ValueObjectChoicePanel, PrismValueObjectSelectorPanel or something better
 *
 * @param <T>
 * @param <O> common superclass for all the options of objects that this panel should choose
 */
public class ValueChoosePanel<T, O extends ObjectType> extends BasePanel<T> {

  private static final long serialVersionUID = 1L;

	private static final Trace LOGGER = TraceManager.getTrace(ValueChoosePanel.class);

    private static final String ID_TEXT_WRAPPER = "textWrapper";
    private static final String ID_TEXT = "text";
    private static final String ID_FEEDBACK = "feedback";
    private static final String ID_EDIT = "edit";

    protected static final String MODAL_ID_OBJECT_SELECTION_POPUP = "objectSelectionPopup";
 
    private Collection<Class<? extends O>> types;

    public ValueChoosePanel(String id, IModel<T> value, List<PrismReferenceValue> values, boolean required, Collection<Class<? extends O>> types) {
        super(id, value);
        setOutputMarkupId(true);
        
        this.types = types;

        initLayout(value, values, required, types);
    }

    private void initLayout(final IModel<T> value, final List<PrismReferenceValue> values, final boolean required, Collection<Class<? extends O>> types) {


        WebMarkupContainer textWrapper = new WebMarkupContainer(ID_TEXT_WRAPPER);

        textWrapper.setOutputMarkupId(true);

        TextField<String> text = new TextField<String>(ID_TEXT, createTextModel(value));
        text.add(new AjaxFormComponentUpdatingBehavior("blur") {
        	private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
            }
        });
        text.setRequired(required);
        text.setEnabled(false);
        textWrapper.add(text);

        FeedbackPanel feedback = new FeedbackPanel(ID_FEEDBACK, new ComponentFeedbackMessageFilter(text));
        textWrapper.add(feedback);

        AjaxLink<String> edit = new AjaxLink<String>(ID_EDIT) {
        	private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                editValuePerformed(values, target);
            }
        };
        textWrapper.add(edit);
        add(textWrapper);

    }

    protected void replaceIfEmpty(Object object) {
        ObjectReferenceType ort = ObjectTypeUtil.createObjectRef((ObjectType) object);
        ort.setTargetName(((ObjectType) object).getName());
        getModel().setObject((T) ort.asReferenceValue());

    }


    protected ObjectQuery createChooseQuery(List<PrismReferenceValue> values) {
        ArrayList<String> oidList = new ArrayList<>();
        ObjectQuery query = new ObjectQuery();
//TODO we should add to filter currently displayed value
//not to be displayed on ObjectSelectionPanel instead of saved value
//		for (PrismReferenceValue ref : values) {
//			if (ref != null) {
//				if (ref.getOid() != null && !ref.getOid().isEmpty()) {
//					oidList.add(ref.getOid());
//				}
//			}
//		}

//		if (isediting) {
//			oidList.add(orgModel.getObject().getObject().asObjectable().getOid());
//		}

        if (oidList.isEmpty()) {
            return null;
        }

        ObjectFilter oidFilter = InOidFilter.createInOid(oidList);
        query.setFilter(NotFilter.createNot(oidFilter));

        return query;
    }

    /**
     * @return css class for off-setting other values (not first, left to the
     * first there is a label)
     */
    protected String getOffsetClass() {
        return "col-md-offset-4";
    }

    protected IModel<String> createTextModel(final IModel<T> model) {
        return new AbstractReadOnlyModel<String>() {
        	private static final long serialVersionUID = 1L;
            @Override
            public String getObject() {
                T ort = (T) model.getObject();

                if (ort instanceof PrismReferenceValue) {
                    PrismReferenceValue prv = (PrismReferenceValue) ort;
                    return prv == null ? null : (prv.getTargetName() != null ?
                            (prv.getTargetName().getOrig() + (prv.getTargetType() != null ? ": " + prv.getTargetType().getLocalPart() : "") )
                            : prv.getOid());
                } else if (ort instanceof ObjectViewDto) {
                    return ((ObjectViewDto) ort).getName();
                }
                return ort != null ? ort.toString() : null;

            }
        };
    }

    protected void editValuePerformed(List<PrismReferenceValue> values, AjaxRequestTarget target) {
    	List<QName> supportedTypes = WebComponentUtil.resolveObjectTypesToQNames(types, getPageBase().getPrismContext());
    	ObjectFilter filter = createChooseQuery(values) == null ? null : createChooseQuery(values).getFilter();
    	Class<O> defaultType = (Class<O>) types.iterator().next();
		ObjectBrowserPanel<O> objectBrowserPanel = new ObjectBrowserPanel<O>(getPageBase().getMainPopupBodyId(), defaultType, supportedTypes, false, getPageBase(), filter) {
    		private static final long serialVersionUID = 1L;

			@Override
    		protected void onSelectPerformed(AjaxRequestTarget target, O object) {
    			getPageBase().hideMainPopup(target);
    			ValueChoosePanel.this.choosePerformed(target, object);
    		}
			
			
    	};
    	
    	getPageBase().showMainPopup(objectBrowserPanel, target);
    	
    }

    /*
     * TODO - this method contains check, if chosen object already is not in
     * selected values array This is a temporary solution until we well be able
     * to create "already-chosen" query
     */
    protected void choosePerformed(AjaxRequestTarget target, O object) {
        choosePerformedHook(target, object);

        if (isObjectUnique(object)) {
            replaceIfEmpty(object);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("New object instance has been added to the model.");
        }
        target.add(get(ID_TEXT_WRAPPER));
    }


    protected boolean isObjectUnique(O object) {

        PrismReferenceValue old = (PrismReferenceValue) getModelObject();
        if (old == null || old.isEmpty()) {
            return true;
        }
        if (old.getOid().equals(object.getOid())) {
            return false;
        }
      
        return true;
    }


    /**
     * A custom code in form of hook that can be run on event of choosing new
     * object with this chooser component
     */
    protected void choosePerformedHook(AjaxRequestTarget target, O object) {
    }

}