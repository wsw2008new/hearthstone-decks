angular.module('hearthstoneApp', []).controller('IndexController',
		[ '$scope', '$http', function($scope, $http) {

			$scope.diffOneList = function() {
				$http.get('/api/decklistdiff').success(function(data) {
					$scope.diffs = data;
				})
			}

			$scope.diffAll = function() {
				$http.get('/api/deckdiffall').success(function(data) {
					$scope.diffs = data;
				})
			}
			
			$scope.diffAll();

		} ]);