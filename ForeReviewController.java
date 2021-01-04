package com.sxk.mall.controller.force;

import com.sxk.mall.controller.BaseController;
import com.sxk.mall.entity.*;
import com.sxk.mall.service.*;
import com.sxk.mall.util.PageUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

@Controller
public class ForeReviewController extends BaseController {
   private UserService userService;

   private ProductOrderItemService productOrderItemService;

   private ProductOrderService productOrderService;

   private ProducService producService;

   private ReviewService reviewService;

   private ProducImageService producImageService;
    //转到前台Mall-评论添加页


    @RequestMapping(value = "review/{orderItem_id}",method = RequestMethod.GET)
    public String goToPage(HttpSession session, Map<String,Object> map,
                           @PathVariable("orderItem_id") Integer orderItem_id){
        Object userId=checkUser(session);
        User user;
        if(userId!=null){
            user=userService.get(Integer.parseInt(userId.toString()));
            map.put("user",user);
        }else {
            return "redirect:/login";
        }
        ProductOrderItem orderItem=productOrderItemService.get(orderItem_id);
        if(orderItem==null){
            return "redirect:/order/0/10";
        }
        if(!orderItem.getProductOrderItemUser().getUserId().equals(userId)){
            return "redirect:/order/0/10";
        }
        if(orderItem.getProductOrderItemOrder()==null){
            return "redirect:/order/0/10";
        }
        ProductOrder order=productOrderService.get(orderItem.getProductOrderItemOrder().getProductOrderId());
        if(order==null||order.getProductOrderStatus()!=3){
            return "redirect:/order/0/10";
        }
        if(reviewService.getTotalByOrderItemId(orderItem_id)>0){
            return "redirect:/order/0/10";
        }
        Product product=producService.selectOne(orderItem.getProductOrderItemProduct().getProductId());
        product.setProductReviewCount(reviewService.getTotalByProductId(product.getProductId()));
        product.setSingleProductImageList(producImageService.getList(product.getProductId(),(byte) 0,new PageUtil(0,1)));
        orderItem.setProductOrderItemProduct(product);
        map.put("orderItem",orderItem);
        return "fore/addReview";
    }
//添加一条评论

    @RequestMapping(value = "review",method = RequestMethod.POST)
    public String addReview(HttpSession session, Map<String,Object> map,
                            @RequestParam Integer orderItem_id,
                            @RequestParam String reviewContent) throws UnsupportedEncodingException {
        Object userId=checkUser(session);
        User user;
        if(userId!=null){
            user=userService.get(Integer.parseInt(userId.toString()));
            map.put("user",user);
        }else {
            return "redirect:/login";
        }
        ProductOrderItem orderItem=productOrderItemService.get(orderItem_id);
        if(orderItem==null){
            return "redirect:/order/0/10";
        }
        if(!orderItem.getProductOrderItemUser().getUserId().equals(userId)){
            return "redirect:/order/0/10";
        }
        if(orderItem.getProductOrderItemOrder()==null){
            return "redirect:/order/0/10";
        }
        ProductOrder order=productOrderService.get(orderItem.getProductOrderItemOrder().getProductOrderId());
        if(order==null||order.getProductOrderStatus()!=3){
            return "redirect:/order/0/10";
        }
        if(reviewService.getTotalByOrderItemId(orderItem_id)>0){
            return "redirect:/order/0/10";
        }

        Review review=new Review();
        review.setReviewProduct(orderItem.getProductOrderItemProduct());
        review.setReviewContent(reviewContent);
        review.setReviewCreateDate(new Date());
        review.setReviewUser(user);
        review.setReviewOrderItem(orderItem);
        Boolean un=reviewService.add(review);
        if(!un){
            throw new RuntimeException();
        }
        return "redirect:/product/"+orderItem.getProductOrderItemProduct().getProductId();
    }

}
