var pollIDs;
$.ajax({
    type: "POST",
    url: '/ppolls',
    complete: function(data) {
        if (JSON.parse(data.responseText) != null) {
            pollIDs = JSON.parse(data.responseText)
            console.log(pollIDs)
        }
        $("#chart").height('0em');
    }
});
for (index = 0; index < pollIDs.length; ++index) {
    $.ajax({
        type: "POST",
        url: '/ppollinfo',
        data: {
            'poll_id': pollIDs[index]
        },
        complete: function(data) {
            console.log(data)
        }
    });
    $("#myDropdown").append('<a href="#" id=' + pollIDs[index] + '>' + 'Test' + '</a>') //NEED TO FINISH
}

/* When the user clicks on the button,
toggle between hiding and showing the dropdown content */
function myFunction() {
    document.getElementById("myDropdown").classList.toggle("show");
}

// Close the dropdown menu if the user clicks outside of it
window.onclick = function(event) {
    if (!event.target.matches('.dropbtn')) {
        var dropdowns = document.getElementsByClassName("dropdown-content");
        var i;
        for (i = 0; i < dropdowns.length; i++) {
            var openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
        if (pollIDs.includes(event.target.id)) {
            pPopulate(event.target.id)
        }

    }
}

function pPopulate(pollID) {
    //AJAX GET REQUEST TO GET STUFF
    $("#pTitle").val("")
    $("#pType").val("")
    $("#chart").val("")
    $("#demographic").val("")
    $("#stats").val("")
    $("#pTitle").val("Chart Title")
    $("#demographic").val("Gender - Male")
    $("#stats").val("477 Responses")
    $("#chart").height('40em');
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
        //"startDuration": 0,
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
