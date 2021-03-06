/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyacore.report.builder;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.report.CohortReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for report builders which build cohort reports - i.e. one row-per-patient dataset
 */
public abstract class AbstractCohortReportBuilder extends AbstractReportBuilder {

	/**
	 * @see AbstractReportBuilder#getParameters(org.openmrs.module.kenyacore.report.ReportDescriptor)
	 */
	

	@Override
	protected List<Parameter> getParameters(ReportDescriptor descriptor) {
		return Arrays.asList();
	}
	
	/**
	 * @see AbstractReportBuilder#buildDataSets(org.openmrs.module.kenyacore.report.ReportDescriptor, org.openmrs.module.reporting.report.definition.ReportDefinition)
	 */
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor descriptor, ReportDefinition rd) {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition(descriptor.getName() + " DSD");
		dsd.addParameters(rd.getParameters()); // Same parameters as report

		Mapped<CohortDefinition> cohort = buildCohort((CohortReportDescriptor) descriptor, dsd);

		dsd.addRowFilter(cohort);

		addColumns((CohortReportDescriptor) descriptor, dsd);

		// Map all parameters straight through
		return Arrays.asList(new Mapped<DataSetDefinition>(dsd, Mapped.straightThroughMappings(dsd)));
	}

	/**
	 * Builds and maps the cohort to base this cohort report on
	 * @param descriptor the report descriptor
	 * @param dsd the data set definition
	 * @return the mapped cohort definition
	 */
	protected abstract Mapped<CohortDefinition> buildCohort(CohortReportDescriptor descriptor, PatientDataSetDefinition dsd);

	/**
	 * Override this if you don't want the default (HIV ID, name, sex, age)
	 * @param dsd this will be modified by having columns added
	 */
	protected void addColumns(CohortReportDescriptor report, PatientDataSetDefinition dsd) {
		addStandardColumns(report, dsd);
	}

	/**
	 * Adds the standard patient list columns
	 * @param dsd the data set definition
	 */
	protected void addStandardColumns(CohortReportDescriptor report, PatientDataSetDefinition dsd) { 
		DataDefinition nameDef =new ConvertedPersonDataDefinition();
		DataConverter nameFormattergivenname = new ObjectFormatter("{givenName}");
		nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormattergivenname);
		
		dsd.addColumn("id", new PatientIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "");
	    PersonAttributeType phoneNumber = Context.getPersonService().getPersonAttributeTypeByUuid("b2c38640-2603-4629-aebd-3b54f33f1e3a");
		dsd.addColumn("phone", new PersonAttributeDataDefinition("phone",phoneNumber), "", new PropertyConverter(PersonAttribute.class, "value"));
    
		if (report.getDisplayIdentifier() != null) {
			PatientIdentifierType idType = report.getDisplayIdentifier().getTarget();

			DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
			DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(idType.getName(), idType), identifierFormatter);

			dsd.addColumn(idType.getName(), identifierDef, "");
		}
	}
}
