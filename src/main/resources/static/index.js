window.onload = function() {
	var app = new Vue({
		el: '#app',
		data: {
			token: '',
			user: {
				username: ''
			},
			loginForm: {
				username: 'user1',
				password: 'password'
			},
			messages: [],
			text: ''
		},
		methods: {
			login() {
				axios.post('login', 'username='+this.loginForm.username+'&password='+this.loginForm.password)
                  .then(function (response) {
                      console.log(response.data.token)
                      app.token = response.data.token
                      app.user.username = response.data.username
                      app.connection()
                  })
                  .catch(function (error) {
                      console.log(error)
                  })
			},
			send() {
				app.stompClient.send("/app/hello", {}, JSON.stringify({
					'text': app.text,
					'from': app.user.username
				}));
			},
			connection() {
				const socket = new SockJS('/websocket-endpoint');
				app.stompClient = Stomp.over(socket);
				var headers = {
					'Authorization': 'Bearer ' + app.token
				};
				app.stompClient.connect(headers,(frame) => {
					app.stompClient.subscribe('/topic/greetings', (msg) => {
						console.log(msg.body);
						app.messages.push(JSON.parse(msg.body))
					});
				}, (err) => {
				});
			}
		}
	})
}