package org.rundeck.client.api.model

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import spock.lang.Specification

import java.lang.annotation.Annotation

class JobLoadItemSpec extends Specification {
    def "parse job load item with empty job name"() {
        given:
            def jsonText = '''{
    "index": 1,
    "href": "http://rundeck.local:4440/rundeck/api/34/job/896a3c9e-f765-43cc-8e1a-15566fe275fa",
    "id": "896a3c9e-f765-43cc-8e1a-15566fe275fa",
    "permalink": "http://rundeck.local:4440/rundeck/project/asdf/job/show/896a3c9e-f765-43cc-8e1a-15566fe275fa",
    "name": "",
    "group": "Sales",
    "project": "asdf",
    "error": "Job Name is required"
}
'''
            def retrofit = new Retrofit.Builder().baseUrl('http://test').
                    addConverterFactory(JacksonConverterFactory.create()).
                    build()

        when:
            Converter<ResponseBody, JobLoadItem> converter = retrofit.
                    responseBodyConverter(JobLoadItem.class, [] as Annotation[],);
            JobLoadItem result = converter.convert(ResponseBody.create(jsonText, MediaType.parse('application/json')))

        then:
            result != null
            result.name == ''
            result.id == '896a3c9e-f765-43cc-8e1a-15566fe275fa'
            result.permalink == 'http://rundeck.local:4440/rundeck/project/asdf/job/show/896a3c9e-f765-43cc-8e1a-15566fe275fa'
            result.group == 'Sales'
            result.project == 'asdf'
            result.error == 'Job Name is required'


    }
}
