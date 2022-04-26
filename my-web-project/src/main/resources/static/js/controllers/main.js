angular.module('angularJsApp', ['ngResource']).controller('HomeCtrl', ['$scope', '$http', function ($scope, $http) {

    var vm = this;
    vm.response = '';

    $('#key').focus();

    vm.saveData = function () {
        $http.post('/api/data?key=' + encodeURIComponent(vm.key) + '&value=' + encodeURIComponent(vm.value))
            .then(function success(json) {
                console.log(json);
                vm.getKey = vm.key;
                vm.key = "";
                vm.value = "";
                $('#key').focus();
            }, function fail(data) {
                console.log("Fail");
            });
    };

    vm.getData = function () {
        $http.get('/api/data?key=' + encodeURIComponent(vm.getKey))
            .then(function success(response) {
                vm.response = response.data.result;
            }, function fail(data) {
                console.log("Fail");
            });
    };
}]);