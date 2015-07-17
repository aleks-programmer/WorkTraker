var controllers = angular.module('controllers', ['ngRoute', 'ngGrid', 'ngResource', 'ui.bootstrap', 'ngCookies']);

controllers.controller('LoginFormCtrl', ['$location', '$scope', '$rootScope', 'User', 'AuthenticationService',
        function ($location, $scope, $rootScope, User, AuthenticationService) {
            var loginCtrl = this;

            $scope.user = new User();
            $scope.login = function() {
                AuthenticationService.ClearCredentials();
                User.get({userName:$scope.user.userName}, function(u){
                    if(u.userID) {
                        AuthenticationService.SetCredentials($scope.user.userName);
                        var path = '/userForm/' + u.userID;
                        $rootScope.$emit('redirectEvent', path);
                    }
                    else {
                        $scope.user.$save(function(user1, putResponseHeaders) {
                            var path = '/userForm/' + user1.userID;
                            AuthenticationService.SetCredentials(user1.userName);
                            $rootScope.$emit('redirectEvent', path);
                        });
                    }
                });
            };
        }]
);
controllers.controller('UserFormCtrl', ['$location', '$scope', '$rootScope','$routeParams',
        'User', 'Task', 'Work', 'AuthenticationService',
        function ($location, $scope, $rootScope, $routeParams, User, Task, Work, AuthenticationService) {
            var userFormCtrl = this;
            $scope.sortInfo = {fields: ['taskName'], directions: ['asc']};
            $scope.works = {currentPage: 1};
            $scope.userTask = null;
            $scope.user = null;

            (function() {
                $scope.userTask = new Task();
                $scope.user = User.get({userId:$routeParams.userId});
            })();


            $scope.startWork = function() {
                console.log('start');
                userFormCtrl.saveTask();
            };

            $scope.exit = function() {
                AuthenticationService.ClearCredentials();
                var path = '/loginForm';
                $rootScope.$emit('redirectEvent', path);
            };

            $scope.gridOptions = {
                data: 'works.list',
                useExternalSorting: true,
                sortInfo: $scope.sortInfo,

                columnDefs: [
                    { field: 'userName', displayName: 'Имя Юзера' },
                    { field: 'taskName', displayName: 'Имя работы' },
                    { field: 'workTime', displayName: 'Время в сек.' },
                    { field: 'workStatus', displayName: 'Статус' }
                ],

                multiSelect: false,
                selectedItems: []
            };

            $scope.$on('ngGridEventSorted', function (event, sortInfo) {
                $scope.sortInfo = sortInfo;
            });

            $scope.$watch('sortInfo.fields[0]', function () {
                $scope.refreshGrid();
            }, true);

            userFormCtrl.saveTask = function() {
                Task.get({taskName:$scope.userTask.taskName}, function(ut) {
                    if(ut.taskID) {
                        $scope.task = ut;
                        var path = '/taskForm/user/' + $scope.user.userID + '/task/' + ut.taskID;
                        $rootScope.$emit('redirectEvent', path);
                    } else {
                        console.log('save task');
                        $scope.userTask.$save(function(task, putResponseHeaders) {
                            $scope.task = task;
                            var path = '/taskForm/user/' + $scope.user.userID + '/task/' + task.taskID;
                            $rootScope.$emit('redirectEvent', path);
                        });
                    }
                });
            };



            $scope.refreshGrid = function () {
                var listWorks = {
                    page: $scope.works.currentPage,
                    sortFields: $scope.sortInfo.fields[0],
                    sortDirections: $scope.sortInfo.directions[0]
                };

                Work.get(listWorks, function (data) {
                    $scope.works = data;
                })
            };
        }]
);

controllers.controller('TaskFormCtrl', ['$scope', '$interval', '$rootScope', '$routeParams',
    '$location', 'User', 'Task', 'Work', '$window', 'AuthenticationService',
    function($scope, $interval, $rootScope, $routeParams, $location, User, Task, Work, $window, AuthenticationService) {
        var theController = this;
        $scope.intervalId = null;
        $scope.currentTime = '';
        $scope.seconds = null;
        $scope.startTime = null;
        $scope.endTime = null;
        $scope.workName = null;
        $scope.interval = 1;
        $scope.work = null;
        $scope.user = null;
        $scope.task = null;
        $scope.duration = moment.duration({
            'seconds': 0,
            'hour': 0,
            'minutes': 0
        });

        (function() {
            console.log('init ');
            User.get({userId:$routeParams.userId}, function(u) {
                $scope.user = u;
                Task.get({userTaskId:$routeParams.taskId}, function(t) {
                    $scope.task = t;
                    Work.get({userId:$scope.user.userID, taskId:$scope.task.taskID}, function(w) {
                        if(w.userID && w.taskID) {
                            console.log('init and saveWorkOnStart1');
                            $scope.checkWork(w);
                            $scope.duration = moment.duration(w.workTime, 'seconds');
                            $scope.work = w;
                            $scope.saveWorkOnStart($scope.work);
                        } else{
                            $scope.duration = moment.duration(0, 'seconds');
                            $scope.work = new Work({userID:$scope.user.userID, taskID:$scope.task.taskID});
                            console.log('init and saveWorkOnStart2');
                            $scope.saveWorkOnStart($scope.work);
                        }
                    });
                });
            });
        })();

        $scope.checkWork = function(w) {
            if(w.workStatus !== 'WORKED') {
                $window.alert('Извините! Но данная работа уже выполняется!');
                var path = '/userForm/' + $scope.user.userID;
                $rootScope.$emit('redirectEvent', path);
            }
        };

        $window.onbeforeunload = function (evt) {
            var message = 'Есть несохраненные изменения!';
            if (!evt) {
                evt = $window.event;
            }
            if (evt) {
                evt.returnValue = message;
            }
            return message;
        };

        $scope.saveWorkOnStart = function(w) {
            $scope.work = w;
            $scope.work.workTime = $scope.duration.asSeconds();
            $scope.work.workStatus = 'IN PROGRESS';
            $scope.work.$save(function(w) {
                $scope.$emit('startEvent');
            });
        };

        $scope.$on('startEvent', function (event) {

            $scope.start();
        });

        $scope.tick = function() {
            console.log('duration.asSeconds(): ' +$scope.duration.asSeconds());
            console.log('interval: ' +$scope.interval);
            $scope.duration = moment.duration($scope.duration.asSeconds() + $scope.interval, 'seconds');
            $scope.currentTime = Math.round($scope.duration.days()) + ':'
            + Math.round($scope.duration.hours()) + ':'
            + Math.round($scope.duration.minutes()) + ':'
            + Math.round($scope.duration.seconds());
            console.log('$scope.currentTime: ' +$scope.currentTime);
        };

        $scope.start = function() {
            $scope.intervalId = $interval($scope.tick, 1000);
        };
        $scope.resetInterval = function() {
            $interval.cancel($scope.intervalId);
        };

        $scope.saveWork = function() {
            $scope.work = new Work({userID:$scope.user.userID, taskID:$scope.task.taskID});
            $scope.work.workTime = $scope.duration.asSeconds();
            $scope.work.workStatus = 'WORKED';
            $scope.work.$save(function(w) {
                var path = '/userForm/' + $scope.user.userID;
                $rootScope.$emit('redirectEvent', path);
            });
        };
        $scope.stop = function() {
            $scope.resetInterval();
            $scope.seconds = $scope.duration.asSeconds();
            $scope.saveWork();
        };
    }]);

controllers.factory('User', function ($resource) {
    return $resource('taskService/service/user/:userId');
});
controllers.factory('Work', function ($resource) {
    return $resource('taskService/service/user/:userId/task/:taskId');
});
controllers.factory('Task', function ($resource) {
    return $resource('taskService/service/task/:userTaskId');
});
controllers.factory('AuthenticationService',
    ['$http', '$cookieStore', '$rootScope',
        function($http, $cookieStore, $rootScope) {
            var service = {};

            service.SetCredentials = SetCredentials;
            service.ClearCredentials = ClearCredentials;

            return service;

            function SetCredentials(username) {
                var authdata = Base64.encode(username);

                $rootScope.globals = {
                    currentUser: {
                        username: username,
                        authdata: authdata
                    }
                };

                $http.defaults.headers.common['Authorization'] = 'Basic ' + authdata;
                $cookieStore.put('globals', $rootScope.globals);
            }

            function ClearCredentials() {
                $rootScope.globals = {};
                $cookieStore.remove('globals');
                $http.defaults.headers.common.Authorization = 'Basic ';
            }


        }]);

// Base64 encoding service
var Base64 = {

    keyStr: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=',

    encode: function (input) {
        var output = "";
        var chr1, chr2, chr3 = "";
        var enc1, enc2, enc3, enc4 = "";
        var i = 0;

        do {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);

            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;

            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }

            output = output +
            this.keyStr.charAt(enc1) +
            this.keyStr.charAt(enc2) +
            this.keyStr.charAt(enc3) +
            this.keyStr.charAt(enc4);
            chr1 = chr2 = chr3 = "";
            enc1 = enc2 = enc3 = enc4 = "";
        } while (i < input.length);

        return output;
    },

    decode: function (input) {
        var output = "";
        var chr1, chr2, chr3 = "";
        var enc1, enc2, enc3, enc4 = "";
        var i = 0;

        var base64test = /[^A-Za-z0-9\+\/\=]/g;
        if (base64test.exec(input)) {
            window.alert("There were invalid base64 characters in the input text.\n" +
            "Valid base64 characters are A-Z, a-z, 0-9, '+', '/',and '='\n" +
            "Expect errors in decoding.");
        }
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

        do {
            enc1 = this.keyStr.indexOf(input.charAt(i++));
            enc2 = this.keyStr.indexOf(input.charAt(i++));
            enc3 = this.keyStr.indexOf(input.charAt(i++));
            enc4 = this.keyStr.indexOf(input.charAt(i++));

            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;

            output = output + String.fromCharCode(chr1);

            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }

            chr1 = chr2 = chr3 = "";
            enc1 = enc2 = enc3 = enc4 = "";

        } while (i < input.length);

        return output;
    }
};