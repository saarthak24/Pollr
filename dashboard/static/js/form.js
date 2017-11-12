$('.message a').click(function() {
    $('form').animate({
        height: "toggle",
        opacity: "toggle"
    }, "slow");
});

function register() {
    fullName = $('#fullName').val();
    email = $('#email').val();
    username = $('#username').val();
    zipCode = $('#zip').val();
    password = $('#password').val();
    confirmPassword = $('#confirmPassword').val();
    if (!(username.length == 0 || fullName.length == 0 || password.length == 0 || confirmPassword == 0)) {
        if (password == confirmPassword) {
            if (fullName.indexOf(' ') >= 0) {
                $.ajax({
                    type: "POST",
                    url: '/register',
                    data: {
                        "fullName": fullName,
                        "email": email,
                        "username": username,
                        "password": password,
                        "zip": zipCode
                    },
                    complete: function() {
                        $("#fullName").val("");
                        $("#email").val("");
                        $("#username").val("");
                        $("#zip").val("");
                        $("#password").val("");
                        $("#confirmPassword").val("");
                        $('form').animate({
                            height: "toggle",
                            opacity: "toggle"
                        }, "slow");
                    },
                    dataType: 'json'
                });
            }
        }
    }
}
