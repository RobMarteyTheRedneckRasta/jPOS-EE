---
ID: 178
post_title: >
  API integration for MJ Traceability
  reporting in WA State.
author: admin
post_excerpt: ""
layout: page
permalink: >
  http://cannabisys.com/hello-fellow-cannabis-industry-entrepreneurs/jpos-cannapos-project/api-integration-for-mj-traceability-reporting-in-wa-state/
published: true
post_date: 2017-11-11 08:30:34
---
As Washington continues to evolve to a new Mj reporting platform. Leafdata continues to work on their implementations with additional functionality. Per recent news, Mj Freeway didn't meet their Oct. end-of-month deadline. But they are still working on the full implementation.

<hr />

<h3>For anyone wanting to learn this process and data points, here is the state info for third party integrators - <a href="https://lcb.wa.gov/mjtrace/-traceability-third-party-integrators">Read on wa.gov;</a></h3>
The MJFreeway API is a RESTful web service for developers to programmatically interact with MJFreeway's data and real-time delivery management functionality.
<ul>
 	<li>You can add multiple records at once using an array.</li>
 	<li>You can update or delete <b>only 1</b> record at a time.</li>
 	<li>Data exchanged between clients and the API is done with JSON or XML over HTTPS.</li>
 	<li>Https path for the MJFreeway API
<ul>
 	<li>https://watest.leafdatazone.com/api/v1.</li>
</ul>
</li>
 	<li>Fields such as <b>global_mme_id</b> and <b>global_user_id</b> will be auto created and updated.
<ul>
 	<li>You will not need to enter these fields on a Create Request.</li>
</ul>
</li>
</ul>
If you have questions about using the API, want to share some feedback, or have come across a bug you'd like to report, write us an email at support@MJFreeway.com or submit a request through our Support Center.
<h3><em>CSV Only Limitations</em></h3>
Due to the complexity of the Inventory Transfer and Sales models, which both contain many sub models, CSV add can only do 1 at a time with many sub models;
<ul>
 	<li>You can have 1 transfer with many transfer items.</li>
 	<li>You can have 1 sale with many sale items.</li>
</ul>
<h3>Sending Messages</h3>
Messages need to have EMAIL and PASSWORD in the request header. Here a few examples to connect to the API:
<pre>function getAreas() {
    $("#rs").text('processing...');

    var request = $.ajax({
        type: 'GET,
        contentType: 'application/json',
        dataType: 'json',
        data : '',
        url: 'https://watest.leafdatazone.com/api/v1/areas',
        beforeSend: function (xhr) {
            xhr.setRequestHeader("Authorization", "Basic " + btoa('EMAIL' + ":" + 'PASSWORD'));
    },
    complete: function(response) {
        var jsonPretty = JSON.stringify(response['responseJSON'], null, '\t');
        alert(jsonPretty);
    },
    error: function (xhr, ajaxOptions, thrownError) {
        alert('Error ' + xhr.status + ' ' + thrownError);
    });
}

</pre>
<pre>curl -X GET https://watest.leafdatazone.com/api/v1/areas
    -u "EMAIL:PASSWORD"
    -H "Content-Type: application/json"
</pre>
<h3>Valid Response</h3>
Status codes of 200, 201
<pre>{
	"total": 6,
	"per_page": 100,
	"current_page": 1,
	"last_page": 1,
	"next_page_url": null,
	"prev_page_url": null,
	"from": 1,
	"to": 6,
	"data": [
		{
			"external_id": "1",
			"name": "Grow Room 1",
			"type": "grow room",
			"global_id": "NVFCAA.ARLFLS",
			"global_mme_id": "NVCCAA.MM7PT",
			"global_user_id": "NVCCAA.US7PT"
		}
	]
}
</pre>
<h3>Errors</h3>
Status codes of 401,404, 422, 500
<pre>{
	"error": true,
	"error_message": "Not Found",
	"validation_messages": []
}


<hr />

Additional information:
<a href="https://lcb.wa.gov/mjtrace/reporting">Traceability Reporting </a></pre>