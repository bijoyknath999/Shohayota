package dunkeydev.shohayota;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

  private List<Message> messageList;
  private Activity activity;

  public ChatAdapter(List<Message> messageList, Activity activity) {
    this.messageList = messageList;
    this.activity = activity;
  }

  @NonNull @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(activity).inflate(R.layout.adapter_message_one, parent, false);
    return new MyViewHolder(view);
  }

  @Override public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    String message = messageList.get(position).getMessage();
    boolean isReceived = messageList.get(position).getIsReceived();
     if(isReceived){
       holder.messageReceive.setVisibility(View.VISIBLE);
       holder.messageSend.setVisibility(View.GONE);
       holder.messageReceive.setText(message);

       if (check(message))
       {
         holder.linearLayout.setVisibility(View.VISIBLE);
         holder.Call.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
             Intent callIntent = new Intent(Intent.ACTION_CALL);
             callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             callIntent.setData(Uri.parse("tel:"+message));
             activity.startActivity(callIntent);
           }
         });

         holder.Copy.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
             ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
             ClipData clip = ClipData.newPlainText("phone", message);
             clipboard.setPrimaryClip(clip);
             Toast.makeText(activity, "Number Copied!", Toast.LENGTH_SHORT).show();
           }
         });
       }

     }else {
       holder.messageSend.setVisibility(View.VISIBLE);
       holder.messageReceive.setVisibility(View.GONE);
       holder.messageSend.setText(message);
       holder.linearLayout.setVisibility(View.GONE);
     }
  }


  private boolean check(String message)
  {
    return StringUtils.isNumeric(message);
  }


  @Override public int getItemCount() {
    return messageList.size();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder{

    TextView messageSend;
    TextView messageReceive;
    LinearLayout linearLayout;
    ImageButton Copy, Call;

    MyViewHolder(@NonNull View itemView) {
      super(itemView);
      messageSend = itemView.findViewById(R.id.message_send);
      messageReceive = itemView.findViewById(R.id.message_receive);
      linearLayout = itemView.findViewById(R.id.message_layout);
      Copy = itemView.findViewById(R.id.message_copy);
      Call = itemView.findViewById(R.id.message_call);
    }
  }

}
