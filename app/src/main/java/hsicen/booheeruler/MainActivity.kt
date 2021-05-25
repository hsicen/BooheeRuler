package hsicen.booheeruler;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import hsicen.ruler.BooheeRuler;
import hsicen.ruler.KgNumberLayout;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    KgNumberLayout knl_top_head = (KgNumberLayout) findViewById(R.id.knl_top_head);
    BooheeRuler br_bottom_head = (BooheeRuler) findViewById(R.id.br_bottom_head);

    knl_top_head.bindRuler(br_bottom_head);
  }
}
