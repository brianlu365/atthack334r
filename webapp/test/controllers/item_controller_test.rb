require 'test_helper'

class ItemControllerTest < ActionDispatch::IntegrationTest
  test "should get collect" do
    get item_collect_url
    assert_response :success
  end

end
