var app = angular.module('app', [
    'ngRoute',
    'ngCookies',
    'controllers',
    'ui.bootstrap'
]);



app.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/loginForm', {
                templateUrl: 'partials/loginForm.html',
                controller: 'LoginFormCtrl'
            }).
            when('/userForm/:userId', {
                templateUrl: 'partials/userForm.html',
                controller: 'UserFormCtrl'
            }).
            when('/taskForm/user/:userId/task/:taskId', {
                templateUrl: 'partials/taskForm.html',
                controller: 'TaskFormCtrl'
            }).
            otherwise({
                redirectTo: '/loginForm'
            });

    }]);
app.run(
    ['$rootScope', '$location', '$cookieStore', '$http', function($rootScope, $location, $cookieStore, $http) {
        $rootScope.globals = $cookieStore.get('globals') || {};
        if ($rootScope.globals.currentUser) {
            console.log('currentUserExists');
            $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata; // jshint ignore:line
        }

        $rootScope.$on('$locationChangeStart', function (event, next, current) {
            // redirect to login page if not logged in and trying to access a restricted page
            var restrictedPage = $.inArray($location.path(), ['/loginForm']) === -1;
            var loggedIn = $rootScope.globals.currentUser;
            console.log('before loginForm to');
            if (restrictedPage && !loggedIn) {
                console.log('loginForm to');
                $location.path('/loginForm');
            }
        });

        $rootScope.$on('redirectEvent', function(event, path) {
            $location.path(path);
        });
    }]);