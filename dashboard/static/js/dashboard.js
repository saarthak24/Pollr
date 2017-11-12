$.ajax({
    type: "POST",
    url: '/ppolls',
    complete: function(data) {
        if (data.responseText != null) {
        var pollIDs = JSON.parse(data.responseText)
        for (index = 0; index < pollIDs.length; ++index) {
            $("#pollSelect").append('<option value=' + pollIDs[index] + '>' + 'Test' + '</option>') //NEED TO FINISH
        }
        pPopulate($("#pollSelect").value)
    }
}
});

$("#pollSelect").change(function() {
    pPopulate(this.value); //NEED TO FINISH
});

function pPopulate(pollID) {
    $.ajax({
        type: "GET",
        url: '/ppollinfo',
        complete: function(data) {
            console.log(data)
        }
    });
    //AJAX GET REQUEST TO GET STUFF
    $("#pTitle").val("")
    $("#pType").val("")
    $("#chart").val("")
    $("#demographic").val("")
    $("#stats").val("")
    // $("#pTitle").val()
    // $("#pType").val()
    // $("#chart").val()
    // $("#demographic").val()
    // $("#stats").val()
    //If type is slider --> Create bar graph
    //if type is multiple choice --> Create pie graph
    AmCharts.makeChart("chart", {
        "type": "pie",
        "balloonText": "[[title]]<br><span style='font-size:14px'><b>[[value]]</b> ([[percents]]%)</span>",
        "titleField": "choice",
        "valueField": "votes",
        "fontSize": 12,
        "theme": "default",
        "allLabels": [],
        "balloon": {},
        "titles": [],
        "dataProvider": [{
                "choice": "Czech Republic",
                "votes": 356.9
            },
            {
                "choice": "Ireland",
                "votes": 131.1
            },
            {
                "choice": "Germany",
                "votes": 115.8
            },
            {
                "choice": "Australia",
                "votes": 109.9
            },
            {
                "choice": "Austria",
                "votes": 108.3
            }
        ]
    });
}
