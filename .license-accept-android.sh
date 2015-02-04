function license_accepted_android ()
{
  expect -c "$(cat <<DOC
  set timeout -1;
  spawn android $@;
  expect {
      "Do you accept the license" { exp_send "y\r" ; exp_continue }
      eof
  }
DOC
)"
}